# Local development
To start the webserver run the following alias:
```
clj -M:server
```

To compile styles run the following:

```
npm install;
npx tailwindcss --postcss -i ./src/generator/css/base.less -o ./resources/public/assets/main.css --watch
```

Tailwind will parse project files and automatically generate css for you based on the classes used in clojure application. Automatic reloading has not been completed shadow-cljs or similar might be required for this.

VS-code settings for local style development:
- Install "Tailwind CSS IntelliSense" plugin
- Add new item to 'Files: Associations' setting with key "*.css" and value "tailwindcss"
- Optionally change the value of "Editor: Quick Suggestions" to enable quick suggestions on strings

Known issues:
- using nesting in css files causes vscode to fail it's validation checks. This functionality relies on tailwincss/nesting component. Syntax is valid by that component but it's not supported by linter. Under investigation