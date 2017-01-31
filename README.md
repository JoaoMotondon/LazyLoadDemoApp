# LazyLoadDemoApp

This project demonstrates some topics when using Picasso, Glide, UIL and Fresco libraries. 

They are:

 - Disk and memory caching: how to enable/disable them for each library.
 - Clearing entire cache: how to clear both disk and memory caches for each library.
 - Callbacks: use of callbacks (sometimes referred as target) which gives us more control over the loading process.
 - Cancel downloads on scrolling: avoid waste of network bandwidth by cancelling a download for a view when it is detached from the window.

It contains a nice drawer menu which allows us to easily change between the available download engines, enable/disable cache (disk and/or memory) as well as clear the entire cache with a simple click.

![Demo](https://cloud.githubusercontent.com/assets/4574670/22489232/75604a8a-e7fd-11e6-841f-5dc11a7be9bb.gif)

There is also a manual implementation. It was copied from [this repository](https://github.com/thest1/LazyList) for the comparison purpose. We made just a few adjusts to fit it to the app architecture, but all the credits for this implementation go to the author of the library.
 
You can find detailed information about this project [on my blog](http://androidahead.com/2017/01/28/lazy-loading-of-images/)

# License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
