# Local development
To start the webserver run the following alias:
```
clj -M:serve
```

To compile styles run the following:

```
npm install;
npx tailwindcss -i ./src/generator/css/base.css -o ./resources/public/assets/main.css --watch
```

Tailwind will parse project files and automatically generate css for you based on the classes used in clojure application. Automatic reloading has not been completed shadow-cljs or similar might be required for this.

