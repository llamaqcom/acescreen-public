# AceScreen Public Components

AceScreen is an app that prevents the Android device from going to sleep based on the current needs of the user. It offers two modes of operation which allow the app to adapt to almost any situation in everyday life. In automatic mode, AceScreen uses sensors and other available information to get the job done. Sometimes you may want the screen not to turn off under any circumstances. For such cases, manual mode is the right thing.

Our team has been contributing to the world of open source software for quite some time now. And we made the decision to publish some of the most security-critical parts of our application source code. This will allow anyone who wants to study the source code, express their thoughts, and, if desired, contribute to it.

At the moment, the following application components are publicly available:

- **Accessibility Service.** Accessibility Service access is the only way on most devices to effectively turn off the screen and not interfere with biometric unlocking (such as fingerprint unlock or smart lock). AceScreen uses the Accessibility Service for this feature to work.
- **Root Access.** This method actively and reliably turns off the screen. This method does not interfere with biometric unlocking (such as smart lock or fingerprint unlock). And this is the only method that allows you to choose what to do if some app or game prevents the screen from turning off.
- Some minor helper classes and modules.

The main idea is to isolate the program code with elevated privileges from other parts of the application as much as possible. It uses an OOP principle known as encapsulation. Thus, we only allow interaction with security-critical capabilities through predefined and extremely limited **public** methods, each of which should perform a narrow and specific task. Due to encapsulation, other parts of the application do not have access to **private** methods, which greatly reduces the possibility of executing unexpected elevated code by mistake.

If you have any ideas or feedback, feel free to open an issue and contribute.

Learn more about AceScreen on the product website: https://www.acescreen.app

AceScreen is available on Google Play: https://play.google.com/store/apps/details?id=com.llamaq.acescreen
