from django.urls import path, include

urlpatterns = [
    path('django/', include('portal.urls')),
]