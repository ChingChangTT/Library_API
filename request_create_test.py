import json
from urllib.error import HTTPError
from urllib.request import Request, urlopen

url = 'http://localhost:8082/api/books'
data = json.dumps({
    'title': 'New Book',
    'author': 'Author Name',
    'isbn': '9789999999999',
    'publishedYear': 2024
}).encode('utf-8')
req = Request(url, data=data, headers={'Content-Type': 'application/json'})
try:
    res = urlopen(req)
    print('STATUS', res.status)
    print(res.read().decode('utf-8'))
except HTTPError as e:
    print('STATUS', e.code)
    print(e.read().decode('utf-8'))
except Exception as e:
    print('EXCEPTION', type(e).__name__, e)
