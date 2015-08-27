angular.module('app').factory('pentahoService', function($http) {

	// create a new object
	var pentahoFactory = {};

	// get pentaho resources
	pentahoFactory.getJcrList = function(solution, path) {
		return $http.get("/pentaho/plugin/fastsync/api/sync/list/jcr?solution=" + solution + "&path=" + path);
	};

	pentahoFactory.syncJcr = function(solution, path, del, delPerm, debug) {
		return $http.get("/pentaho/plugin/fastsync/api/sync/jcr?solution=" + solution + "&path=" + path + "&delete=" + del + "&deletePerm=" + delPerm + "&debug=" + debug);
	};

	// return our entire pentahoFactory object
	return pentahoFactory;

});