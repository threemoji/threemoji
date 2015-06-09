# Threemoji
For people who want to meet new people nearby, Threemoji is a social app that matches individuals with like-minded peers. Unlike Tinder, Threemoji matches people based on the 3 emojis they choose to describe themselves with and chats are all anonymous.

# Overview
![Threemoji][ignition-slide]

Threemoji is the go-to app for lonely hearts who desire simplicity. Unlike other social apps, users do not have to go through the hassle of creating an online profile - simply select three emoji and get matched. Threemoji is also distinct from other anonymous chat apps in that there is some degree of preference matching through the use of emoji, a new and fun way of describing oneself.

We are aiming for Apollo 11. [Ignition presentation][ignition-presentation]

# Planned Features
<br>
<img src="images/prototype/main-screen.gif" width="50%" align="left">
Threemoji displays your chats sorted by their last active time.

You will be automatically matched and a chat will be initiated for you.

>We are still in the process of defining the meaning of match. Currently, our plan is to categorise the various emoji, where emoji of the same category will be highly similar. Each emoji will have several categories and emoji that share a higher number of similar categories will have a higher level of similarity.

>The use of generated aliases such as "Flying Pig" ensures that two people sharing the same set of emoji can be differentiated.

Notifications will prompt you whenever a match happens or when new messages arrive.
<br clear="all">

<br>
<img src="images/prototype/people-nearby-screen.gif" width="50%" align="right">
Threemoji allows you to see people nearby who were not automatically matched with you.

These people are sorted by the extent of match with you.

You can then start a chat with any of them.
>Chats will be encrypted so even the administrators will not be able to read messages.

<br clear="all">

<br>
<img src="images/prototype/side-menu.gif" width="50%" align="left">
You can change your emoji anytime to match with different people.

If you want to match with people farther away, you can change your radius of match.

Chats that are inactive for 3 weeks will be archived and they can be accessed from this side menu.
<br clear="all">

# Milestones
Our goal is to complete an Android application and perhaps an iOS application if time permits.

We've come up with some user stories sorted by level of priority: [User Stories]

# References
* [Proto] - Prototyping web application for making the prototypes above
* [LICEcap] - For recording gifs
* [Emoji One] - Open source emoji
* [Gfycat wordlist] - Words used by Gfycat to generate their URLs

* [Sublime Text 3] - For code editing
* [MarkdownEditing] - Package for Sublime Text for nicer markdown formatting
* [Markdown Preview] - Package for Sublime Text to preview formatted markdown documents locally
* [Markdown Cheatsheet] - For markdown syntax help

* [App Engine Backend] - How to build a mobile app with an App Engine backend
* [Google Cloud Messaging] - How to create an Instant Messaging app using GCM

* [Material Design] - Design guidelines for Android applications
* [Android guides] - Useful guides for Android development
* [Demo app] - Uses the latest Android design library from Google I/O 2015
* [Hex value transparency] - Translucent colors represented by hex values

---
An Orbital 2015 project.

[ignition-slide]: images/ignition/155.png "Threemoji"
[ignition-presentation]: https://youtu.be/osQjStOAci0?t=1h59m10s
[main-screen]: images/prototype/main-screen.gif

[User Stories]: User-Stories.md

[Proto]: http://proto.io
[LICEcap]: http://www.cockos.com/licecap/
[Emoji One]: http://emojione.com/
[Gfycat wordlist]: http://www.reddit.com/r/gfycat/comments/2bkl5j/gfycat_url_generator/

[Sublime Text 3]: http://www.sublimetext.com/3
[MarkdownEditing]: https://packagecontrol.io/packages/MarkdownEditing
[Markdown Preview]: https://packagecontrol.io/packages/Markdown%20Preview
[Markdown Cheatsheet]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet

[App Engine Backend]: https://cloud.google.com/developers/articles/how-to-build-mobile-app-with-app-engine-backend-tutorial/
[Google Cloud Messaging]: http://www.appsrox.com/android/tutorials/instachat/

[Material Design]: http://www.google.com/design/spec/material-design/introduction.html
[Android guides]: http://guides.codepath.com/android
[Demo app]: https://github.com/chrisbanes/cheesesquare

[Hex value transparency]: http://stackoverflow.com/questions/15852122/hex-transparency-in-colors
