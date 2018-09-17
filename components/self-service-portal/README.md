# Self Service Portal UI

## Introduction
The Self-care Portal differs from the other two functional areas. For Registration and Authentication the process is designed to guide the user from one step to another.
Whereas, for the Self-care Portal the process is designed to encourage the user to explore, by having screens in the flow that are interlinked

## Install tools

Download and install nodejs https://nodejs.org

Note: Make sure you have Node version >= 6.0 and NPM >= 3

## Build and Run Development Server
Run the Following Commands to build the source and start the development server

#### 1. Install NPM dependencies

```
cd angular-source
npm install
```

#### 2. Build and run the project on node development server (default on http://localhost:3000)

```
npm run start
```

## Build and generate the `selfservice.war`

### Method 1

#### 1. Build the angular-source project, clean existing content and copy compiled package to `self-service-portal/src/maim/webapp` by running following command

```
npm run build:prod
```

#### 2. Go to project parent folder (`self-service-portal`) and produce the war file

```
cd self-service-portal
mvn clean install
```


### Method 2

#### 1. Open pom.xml of `self-service-portal` project
#### 2. Edit execution plugin configuration `exec-npm-install` and `exec-npm-build` and set `skip` attribute to `false`

```
 <skip>false</skip>
```


## Based on
NPM, Angular 2, Bootstrap 4, Webpack
