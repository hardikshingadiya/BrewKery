# Brewkery

# Download APK : https://drive.google.com/file/d/1Xbnx_KKxbCfXa1WfZGc1bI9o2vvOV9yB

### Native Android Coffee & Bakery Ordering App

Brewkery is a native Android application built with **Kotlin** for the
Clickretina Android Developer assignment. Users can browse a coffee and
bakery menu, customize items, manage a cart, and view order status and
order history.

## Features

-   Browse menu items loaded from the provided REST API.
-   View item details and customize size, milk, and sugar where
    available.
-   View cart items, update quantities, and see subtotal, delivery fee,
    tax, and total.
-   Complete the local checkout flow and view a generated order ticket
    and status.
-   View previous orders during the current app session.
-   Display loading and error states for API requests.
-   Load menu images from remote URLs.

## Tech Stack

-   **Language:** Kotlin
-   **UI:** Android Views and XML layouts
-   **Networking:** Retrofit and Gson
-   **Asynchronous work:** Kotlin Coroutines
-   **Image loading:** Coil
-   **State and app logic:** ViewModel, Repository, CartManager, and
    OrderManager
-   **Testing:** JUnit local unit test

## App Flow

`Menu → Item Details → Cart → Order Status → Order History`

## Run the Project

1.  Clone or download this repository.
2.  Open the project in Android Studio.
3.  Let Gradle sync and download dependencies.
4.  Run the app on an Android emulator or connected device.
5.  Ensure the API base URL points to the API provided for the
    assignment.

## Unit Test

Run the local unit tests from the project root on Windows:

``` powershell
.\gradlew.bat testDebugUnitTest
```

The test report for the current project showed **1 test, 0 failures, 0
errors**. The existing `ExampleUnitTest` checks a basic addition
operation; it is a sanity check rather than coverage of the app's
business logic.

## AI-Assisted Development

### AI tools used

-   **ChatGPT** --- to discuss implementation options, understand
    errors, plan individual screens, review snippets, and help debug the
    app.
-   I also used Android Studio to apply changes, run the app, and verify
    the results. ChatGPT suggestions were treated as assistance, not as
    a substitute for testing.

### Example prompts

I often wrote prompts informally and shared the relevant code,
screenshots, errors, or assignment context. The examples below are
polished versions that represent the kinds of prompts I used; they are
not intended as exact word-for-word transcripts.

1.  **Building a feature while preserving the existing project** \>
    "Help me implement the item-details screen for the Brewkery Android
    assignment using Kotlin and XML. Use the existing API models and
    repository, load the selected item by ID, and support the available
    size, milk, and sugar customizations. Please explain which files
    need to change and avoid rewriting unrelated parts of the project."

2.  **Debugging a UI issue** \> "The cart delivery fee is displaying
    `₹2.50`, but the assignment's design uses dollars. Please identify
    the relevant layout value and show the smallest change needed to
    display `$2.50`. Also help me verify that the cart count updates
    when items are added or removed."

3.  **Verifying the final flow** \> "Help me verify the complete
    Brewkery flow from menu to item details, cart, checkout, order
    status, and order history. Point out what I should test manually and
    show me how to run the local unit tests in Android Studio."

### One thing AI got right

ChatGPT helped break the assignment into smaller pieces and connect the
menu, item details, cart, checkout, and order-status screens. Working
one screen or issue at a time made the implementation easier to
understand and test.

### One thing AI got wrong or overcomplicated

At one point, the suggested approach included adding Room database
persistence and related setup. For the remaining assignment time, that
added complexity that was not essential to demonstrate the required
ordering flow, and the setup had not been verified against the project's
configuration.

**How I handled it:** I reviewed the trade-off and kept the cart and
order history in simple in-memory managers instead. This let me finish
and test the end-to-end flow without adding an unneeded database
dependency. As a result, cart and order history are not retained after
the app process is restarted.

### What I learned

AI is useful for exploring solutions and debugging, but its suggestions
still need to be checked against the assignment requirements, the
existing codebase, time constraints, and actual app behavior. I shared
context, applied changes in Android Studio, and tested the flow rather
than assuming generated suggestions would work automatically.

## Notes and Limitations

-   The cart and order history use in-memory state and do not persist
    across app restarts.
-   The order status and ticket ID are generated locally for the
    assignment flow; they do not represent a backend order-tracking
    service.
-   The current unit test is a basic example test. Tests for cart
    calculations and order logic would improve coverage.

------------------------------------------------------------------------

**Built with Kotlin and Android Studio.**
