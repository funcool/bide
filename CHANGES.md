# Changelog #

## Version 1.7.0

Date: 2021-05-02

- Update deps and compatibility issues with latest gclosure library.


## Version 1.6.0

Date: 2017-10-07

- Remove double navigation hook on start (thanks to @pepe).


## Version 1.5.1

Date: 2017-07-26

- Fix compatibility with latest cljs compiler.


## Version 1.5.0

Date: 2017-04-25

- Add the ability to provide customized instance of `goog.history.Html5History`
  that would be used to manage history events.


## Version 1.4.0

Date: 2017-01-12

- Add the ability to stop the router.


## Version 1.3.0

Date: 2017-01-02

- Add support for html5 push-state (thanks to Html5History from google
  closure library).
- Remove unused code from internal implementation.
- Performance improvements (2x or 3x on `match` function).


## Version 1.2.2 ##

Date: 2016-12-25

- Add support for URL replace.


## Version 1.2.1 ##

Date: 2016-12-07

- Add missing dependency declaration to `goog.object` ns.


## Version 1.2.0 ##

Date: 2016-12-07

- Add support to resolve urls with query string encoding.


## Version 1.1.0 ##

Date: 2016-12-07

- Add support to query params parsing.


## Version 1.0.6 ##

Date: 2016-11-27

- Fix wrong handling of `-` char in url params.


## Version 1.0.5 ##

Date: 2016-09-30

- Add IPathRepr impl for `nil` and `keyword`.


## Version 1.0.4 ##

Date: 2016-08-28

- Improve handling of paths with same name but different.


## Version 1.0.3 ##

Date: 2016-08-26

- Disable async call of the initial navigate event.


## Version 1.0.2 ##

Date: 2016-08-26

- Add missing IPathRepr for string.


## Version 1.0.1 ##

Date: 2016-08-26

- Minor internal change.


## Version 1.0.0 ##

Date: 2016-08-26

- Initial release
