# Amplify Fire TV App - My Favorite Videos

A modern Fire TV application built with Kotlin and AWS Amplify, demonstrating how to create a video playing app with user authentication, content management, and favorite functionality.

## Overview

This project showcases how to build a Fire TV application that leverages AWS Amplify for backend services. The app provides a video browsing experience with features like:

- Video browsing with card-based UI
- Use of Leanback library for TV-optimized UI components
- User authentication using AWS Cognito
- Video favorites system
- Video playback with Exoplayer

## Offloading concerns to AWS Amplify

The app leverages several AWS services through Amplify:

1. **Authentication (AWS Cognito)**
   - User sign-in/sign-up
   - Session management

2. **Storage (S3)**
   - Content metadata (`cards.json`)
   - URL references to thumbnail images
   - URL references to videos

3. **Data (AppSync/GraphQL)**
   - Favorite management

## Technical Implementation

### Data Management
- `CardDataProvider`: Manages video content metadata
- S3 integration for content storage

### UI Components
- `VideoCardPresenter`: Handles video card presentation
- `VideoDetailsFragment`: Manages video details view

### Authentication
- `AuthStateManager`: Centralizes auth state management

## Getting Started

### 1. Clone the repository

### 2. Create an AWS IAM user with resource provisioning permissions

### 3. Spin up an Amplify sandbox

```
~/project$ npx ampx sandbox --profile PROFILE_CONFIGURED_VIA_AWS_CLI
```

### 4. Generate `amplify_outputs.json` file

```
~/project$ npx ampx \
           --profile PROFILE_CONFIGURED_VIA_AWS_CLI generate outputs \
           --stack STACK_NAME_FROM_SANDBOX_COMMAND_OUTPUT \
           --out-dir app/src/main/res/raw
```

### 5. Upload `cards.json` to S3 bucket 

Examine `amplify_outputs.json` for data S3 bucket name.

Create a folder called `data` in the S3 bucket.

Upload `app/src/main/assets/cards.json` to the `data/` folder in the S3 bucket.

### 6. Create a test user in Cognito

Examine `amplify_outputs.json` for Cognito user pool ID.

Create a new user in that user pool.

Use admin privileges to permanently set the user's password (to move the user state beyond "Force change password")

```
$ aws cognito-idp admin-set-user-password \
    --profile PROFILE_CONFIGURED_VIA_AWS_CLI \
    --user-pool-id USER_POOL_ID \
    --username user@example.com \
    --password "21charexamplepassword" \
    --permanent
```

### 7. Open Android Studio

### 8. Build project

### 9. Create a virtual device for Android Emulator

* Device type: **Android TV**
* Screen size: **40 inch**
* Resolution: **1280 x 720 px**
* Navigation Style: **D-Pad**
* Supported Device States: **Landscape** only
* Cameras and Sensors: All unchecked\

Select a system image with an API level of 23 or higher. Recommended: **Q (29)**

### 10. Run project and test in emulator

## Video Sources

This project (in `cards.json`) uses sample videos from the following collection:
https://gist.github.com/jsturgis/3b19447b304616f18657#file-gistfile1-txt 