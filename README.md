# www.cartman.fi website

This repository contains static HTML generator www.cartman.fi website implemented using Clojure. Website data is fetched from Contentful. A running Cloud Run container hosts API endpoints for handling eg. forms and preview mode.

## Configuration files and Environment variables
This project supports configuration via Environment variables or two config files `generator-config.edn` or `sensitive-config.edn` at the root of the project. sensitive-config file should not be committed to git repository.
To successfully run the project the following environment variables must be configured:

```
BASE_URL - string - website's frontpage url ending with /
CONTENTFUL_SPACE - string - contentful's space hash
CONTENTFUL_ENVIRONMENT - string - environment code from contentful
CONTENTFUL_TOKEN - string - contentful's token
```

Following env variables are optional for majority of pages but can be required by other features such as contact form and preview
```
CONTENTFUL_PREVIEW_TOKEN - string - Token used to fetch previews such as draft and changed content.
CONTENTFUL_PREVIEW_SECRET - string - Used to validate that a request is coming from contentful.
NOTION_MESSAGE_DB_ID - string - Database id where contact form messages are pushed
NOTION_INTEGRATION_TOKEN - string - Token used for pushing messages to notion.
CAPTCHA_PUBLIC_KEY - string - Captcha public key from google  
CAPTCHA_PRIVATE_KEY - string - Captcha private key from google. 
```

## Local development
To launch local environment do the following:
1. Launch REPL using deps.edn (no alias required)
2. Open to src/generator/webserver.clj
3. Load/evaluate file
4. run `(start-webserver)` in REPL

Local webserver should now be available in localhost:8000

To stop the server run `(stop-webserver)`

## Launching with command.
You can launch the webserver with 
`clj -M:project/run -webserver` command
alternatively to generate html and sitemap files you can run
`clj -M:project/run -generate` command.

## Frontend customization
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
- Tailwind parser used in the project is not true less parser and might experience issues when processing less files with some expressions in them. These files are still css files with nesting enabled. Less file definition is used here to prevent vscode validation from breaking with the nesting enabled by Tailwindcss.

## Other useful actions

### Sitemap Generation 
Alias project/generate-sitemap triggers sitemap generation defined in sitemap.clj file.
```
clj -M:project/generate-sitemap
```
This creates sitemap.xml file under public directory in the project.

### Update GCP load balancer url-map

```
gcloud compute url-maps import --project www-cartman-fi --global --source=url-map.yaml site
```
