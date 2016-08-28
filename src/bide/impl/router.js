/**
 * router
 *
 * @author Andrey Antukh <niwi@niwi.nz>, 2016
 * @license BSD License <https://opensource.org/licenses/BSD-2-Clause>
 */

goog.provide("bide.impl.router");
goog.require("bide.impl.path");

goog.scope(function() {
  var _path = bide.impl.path;

  /**
   * Main router class.
   *
   * @constructor
   * @struct
   */
  function Router() {
    this.items = [];
    this.map = {};
  }

  /**
   * Routing Item class
   *
   * @constructor
   * @struct
   */
  function Route() {
    this.re = null;
    this.name = null;
    this.keys = null;
    this.format = null;
  }

  /**
   * Insert a new route entry to the router.
   * If router is `null` a new router is created.
   *
   * @param {?Router} router
   * @param {string} path
   * @param {!Object} name
   * @param {*} options
   * @return {Router}
   */
  function insert(router, path, name, options) {
    var route = new Route();

    route.keys = [];
    route.re = _path.parse(path, route.keys, options);
    route.format = _path.compile(path);
    route.name = name;

    if (!goog.isDefAndNotNull(router)) {
      router = new Router();
    }

    router.items.push(route);

    if (router.map[name.fqn] === undefined) {
      router.map[name.fqn] = [route];
    } else {
      router.map[name.fqn].push(route);
    }

    return router;
  }

  /**
   * Match a path in the router.
   *
   * @param {!Router} router
   * @param {!string} path
   * @return {Array<*>}
   */
  function match(router, path) {
    var items = router.items;
    var result = null;
    var item = null;

    for (var i=0; i<items.length; i++) {
      item = items[i];
      result = item.re.exec(path);

      if (!goog.isNull(result)) {
        break;
      }
    }

    if (goog.isNull(result)) {
      return null;
    }

    var params = {};
    for (var i=0; i<item.keys.length; i++) {
      var key = item.keys[i];
      var res = result[(i + 1)];
      if (goog.isDefAndNotNull(res)) {
        params[key.name] = res;
      }
    }

    if (isEmpty(params)) {
      params = null;
    }

    return [item.name, params];
  }

  /**
   * Perform a resolve operation on router.
   *
   * @param {!Router} router
   * @param {*} name
   * @param {Object<string,*>} params
   * @return {Array<?>}
   */
  function resolve(router, name, params) {
    var routes = router.map[name.fqn] || null;

    if (!goog.isDefAndNotNull(routes)) {
      return null;
    }

    // If params is empty just check all possible
    // options and return the first one that matches
    // in case contrary check only routes with params
    // because route without params does not raise
    // exceptions causing that they are always elected
    // independently if params are passed or not.
    if (isEmpty(params)) {
      for (var i=0; i<routes.length; i++) {
        try {
          return routes[i].format(params);
        } catch (e) {}
      }
    } else {
      for (var i=0; i<routes.length; i++) {
        if (routes[i].keys.length === 0) {
          continue;
        }

        try {
          return routes[i].format(params);
        } catch (e) {}
      }
    }

    return null;
  }

  /**
   * Check if provided value is an instance of Router
   *
   * @param {*} v
   * @return {boolean}
   */
  function isRouter(v) {
    return v instanceof Router;
  }

  /**
   * Create an empty Router instance.
   *
   * @return {Router}
   */
  function empty() {
    return new Router();
  }

  /**
   * Check if provided obj is empty.
   *
   * @param {Object} obj
   * @return {boolean}
   */
  function isEmpty(obj) {
    for (var x in obj) { return false; }
    return true;
  }

  var module = bide.impl.router;
  module.insert = insert;
  module.match = match;
  module.resolve = resolve;
  module.isRouter = isRouter;
  module.empty = empty;
});
