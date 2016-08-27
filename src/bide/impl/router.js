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
    this.map = new Map();
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
    router.map.set(name.fqn, route);

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

    // TODO: handle repeated

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
    var route = router.map.get(name.fqn, null);

    // console.log(router.map);

    if (!goog.isDefAndNotNull(route)) {
      return null;
    }

    return route.format(params);
  }

  function isRouter(v) {
    return v instanceof Router;
  }

  function empty() {
    return new Router();
  }

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
