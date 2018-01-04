angular.module('app').factory(
		'pentahoService',
		function($http) {

			// create a new object
			var pentahoFactory = {};

			// get pentaho resources
			pentahoFactory.getList = function(api, solution, path, keepFlag,
					debug) {
				return $http.get("/pentaho/plugin/fastsync/api/sync/list/"
						+ api + "?solution=" + solution + "&path=" + path
						+ "&keepNewerFlag=" + keepFlag + "&debug=" + debug);
			};

			pentahoFactory.sync = function(api, solution, path, del, delPerm,
					debug, keepFlag) {
				return $http.get("/pentaho/plugin/fastsync/api/sync/" + api
						+ "?solution=" + solution + "&path=" + path
						+ "&delete=" + del + "&deletePerm=" + delPerm
						+ "&debug=" + debug + "&keepNewerFlag=" + keepFlag);
			};

			// return our entire pentahoFactory object
			return pentahoFactory;

		});