angular.module('app').controller("appCtrl", function ($scope, $http, pentahoService) { 

	$scope.exclude  = [];
	$scope.create   = [];
	$scope.update   = [];
	$scope.delete   = [];
	$scope.preserve = [];
	
	$scope.messageAlert;
	
	$scope.loading = false;
	
	$scope.showFlag = false;
	
	$scope.form = {
		checkboxModel: {
			jcr: {
				'delete': true,
				'deletePerm': true
				
			},
			fs: {
				'delete': true,
				'manifest': false
				
			},
			'keep': true
		},
		'path':'/',
		'debug': false
	};

	$scope.getList = function (switchFlag, solution, path, keepFlag, debug) {

		$scope.loading = true;
		
		var api = "fs";
		
		if (switchFlag) api = "jcr";
		
		pentahoService.getList(api, solution, path, keepFlag, debug).success(function (data) {
			if (data.error == 'false') 
			{
				if (typeof data.create === "string") {
					var x = [];
					x.push(data.create);
					data.create = x;
				}
				if (typeof data.update === "string") {
					var x = [];
					x.push(data.update);
					data.update = x;
				}
				if (typeof data.delete === "string") {
					var x = [];
					x.push(data.delete);
					data.delete = x;
				}
				if (typeof data.exclude === "string") {
					var x = [];
					x.push(data.exclude);
					data.exclude = x;
				}
			
				if (typeof data.preserve === "string") {
					var x = [];
					x.push(data.preserve);
					data.preserve = x;
				}

				$scope.exclude  = data.exclude;
				$scope.create   = data.create;
				$scope.update   = data.update;
				$scope.delete   = data.delete;
				$scope.preserve = data.preserve;

				$scope.excludeSize  = (data.exclude == undefined) ? 0 : data.exclude.length;
				$scope.createSize   = (data.create  == undefined) ? 0 : data.create.length;
				$scope.updateSize   = (data.update  == undefined) ? 0 : data.update.length;
				$scope.deleteSize   = (data.delete  == undefined) ? 0 : data.delete.length;
				$scope.preserveSize = (data.preserve  == undefined) ? 0 : data.preserve.length;
				
				$scope.showFlag = true;
			}
			else
			{
				$scope.showFlag = false;
				setMessageAlert(data.message + ": " + data.error_message, data.error);
			}
				
		}).error(function (data, status) {
			setMessageAlert("Aconteceu um problema: " + data, "true");
		
		}).finally(function() {
			$scope.loading = false;
		});
	};

	$scope.sync = function (switchFlag, solution, path, del, delPerm, debug, keepFlag) {

		$scope.loading = true;
		
		var api = "fs";
		
		if (switchFlag) api = "jcr";

		pentahoService.sync(api, solution, path, del, delPerm, debug, keepFlag).success(function (data) {
			if (data.error == 'false') 
			{
				setMessageAlert(data.message, data.error);
				$scope.getList(switchFlag, solution, path, keepFlag,debug);
			}
			else
			{
				setMessageAlert(data.message + ": " + data.error_message, "true");
			}
				
		}).error(function (data, status) {
			setMessageAlert("Aconteceu um problema: " + data, "true");
		
		}).finally(function() {
			$scope.loading = false;
		});				
	};
	
	$scope.collapse = {};
	$scope.collapse.cr = "collapse";
	$scope.collapse.de = "collapse";
	$scope.collapse.up = "collapse";
	$scope.collapse.ex = "collapse";
	$scope.collapse.pr = "collapse";
	
	$scope.setCollapse = function(id) {
		($scope.collapse[id].length == 0) ? ($scope.collapse[id] = "collapse") : ($scope.collapse[id] = "");
	};
	
	var setMessageAlert = function(msg, error) 
	{
		$("#message-box").removeClass();

		if (error == "true")
			$("#message-box").addClass("alert alert-danger");
		else
			$("#message-box").addClass("alert alert-success");
		
		$("#message-alert").html(msg);

		$("#message-box").alert();
		$("#message-box").fadeTo(5000, 500).slideUp(500, function(){
			$("#message-box").hide();
		});
	};
	
});