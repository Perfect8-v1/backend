import requests
from django.conf import settings
from django.shortcuts import render, redirect
from django.views.decorators.http import require_http_methods


def require_auth(view_func):
    """Decorator - kräver inloggning via JWT i session."""
    def wrapper(request, *args, **kwargs):
        if not request.session.get('access_token'):
            return redirect('login')
        return view_func(request, *args, **kwargs)
    return wrapper


def api_headers(request):
    """Auth-headers för Spring Boot API-anrop."""
    return {
        'Authorization': f"Bearer {request.session.get('access_token')}",
        'Content-Type': 'application/json',
    }


@require_http_methods(["GET", "POST"])
def login_view(request):
    if request.session.get('access_token'):
        return redirect('dashboard')

    error = None

    if request.method == 'POST':
        email = request.POST.get('email')
        password = request.POST.get('password')

        try:
            response = requests.post(
                f"{settings.ADMIN_SERVICE_URL}/api/auth/login",
                json={'email': email, 'password': password},
                timeout=5,
            )

            if response.status_code == 200:
                data = response.json()
                roles = data.get('user', {}).get('roles', [])

                if 'ADMIN' in roles or 'SUPER_ADMIN' in roles:
                    request.session['access_token'] = data['accessToken']
                    request.session['refresh_token'] = data['refreshToken']
                    request.session['user_email'] = data['user']['email']
                    request.session['user_roles'] = roles
                    return redirect('dashboard')
                else:
                    error = 'Du har inte admin-behörighet.'
            else:
                error = 'Fel email eller lösenord.'

        except requests.exceptions.ConnectionError:
            error = 'Kunde inte ansluta till servern.'
        except Exception as e:
            error = f'Ett fel uppstod: {str(e)}'

    return render(request, 'portal/login.html', {'error': error})


def logout_view(request):
    request.session.flush()
    return redirect('login')


@require_auth
def dashboard(request):
    context = {
        'user_email': request.session.get('user_email'),
        'user_roles': request.session.get('user_roles', []),
    }
    return render(request, 'portal/dashboard.html', context)


@require_auth
def users(request):
    error = None
    users_list = []

    try:
        response = requests.get(
            f"{settings.ADMIN_SERVICE_URL}/api/admin/users",
            headers=api_headers(request),
            timeout=5,
        )
        if response.status_code == 200:
            users_list = response.json()
        else:
            error = f'Kunde inte hämta användare: {response.status_code}'
    except Exception as e:
        error = str(e)

    context = {
        'user_email': request.session.get('user_email'),
        'users': users_list,
        'error': error,
    }
    return render(request, 'portal/users.html', context)
