import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.environ.get('DJANGO_SECRET_KEY', 'change-me-in-production')

DEBUG = os.environ.get('DEBUG', 'False') == 'True'

ALLOWED_HOSTS = ['*']

INSTALLED_APPS = [
    'django.contrib.staticfiles',
    'django.contrib.sessions',
    'django.contrib.messages',
    'portal',
]

MIDDLEWARE = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'config.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [BASE_DIR / 'templates'],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'config.wsgi.application'

DATABASES = {}

SESSION_ENGINE = 'django.contrib.sessions.backends.signed_cookies'
SESSION_COOKIE_HTTPONLY = True
SESSION_COOKIE_SECURE = True
SESSION_COOKIE_AGE = 3600

STATIC_URL = '/django/static/'
STATIC_ROOT = BASE_DIR / 'staticfiles'

DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'

# Spring Boot API URLs
ADMIN_SERVICE_URL = os.environ.get('ADMIN_SERVICE_URL', 'http://admin-service:8081')
SHOP_SERVICE_URL = os.environ.get('SHOP_SERVICE_URL', 'http://shop-service:8085')
BLOG_SERVICE_URL = os.environ.get('BLOG_SERVICE_URL', 'http://blog-service:8082')
IMAGE_SERVICE_URL = os.environ.get('IMAGE_SERVICE_URL', 'http://image-service:8084')
EMAIL_SERVICE_URL = os.environ.get('EMAIL_SERVICE_URL', 'http://email-service:8083')

JWT_SECRET = os.environ.get('JWT_SECRET', '')
BASE_URL = os.environ.get('BASE_URL', 'https://p8.rantila.com')
