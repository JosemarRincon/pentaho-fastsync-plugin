angular.module('app').factory(
	'pentahoService',
	function ($http) {
		// create a new object
		var pentahoFactory = {};
		// get pentaho resources
		pentahoFactory.getList = function (scope) {
			return $http.get("/pentaho/plugin/fastsync/api/sync/list/"
				+ scope.api + "?solution=" + scope.solution + "&path=" + scope.path
				+ "&keepNewerFlag=" + scope.checkboxModel.keep + "&debug=" + scope.debug + "&withManifest=" + scope.checkboxModel.manifest);
		};
		pentahoFactory.sync = function (scope) {
			return $http.get("/pentaho/plugin/fastsync/api/sync/" + scope.api
				+ "?solution=" + scope.solution + "&path=" + scope.path
				+ "&delete=" + scope.checkboxModel.delete + "&deletePerm=" + scope.checkboxModel.deletePerm
				+ "&debug=" + scope.debug + "&keepNewerFlag=" + scope.checkboxModel.keep + "&withManifest=" + scope.checkboxModel.manifest);
		};
		// return our entire pentahoFactory object
		return pentahoFactory;
	});