angular.module('app').controller("appCtrl", function ($scope, $http, pentahoService) {

	$scope.exclude = [];
	$scope.create = [];
	$scope.update = [];
	$scope.delete = [];
	$scope.preserve = [];

	$scope.messageAlert;
	$scope.api = "fs";

	$scope.loading = false;
	$scope.switchFlag = false;
	$scope.switchFlagTmp = false;
	$scope.hideSync = true;

	$('#switch').on('switchChange.bootstrapSwitch', function(e, state) {
/*
 * console.log(e.target.checked); console.log($scope.switchFlag);
 * console.log(state );
 */
		$scope.switchFlag = !$scope.switchFlag;
		$scope.hideSync = true;
		if(!$scope.switchFlag){
			$scope.checkboxModel.publishCube = false;
		}
		
		
	});


	$scope.path = "/";
	$scope.debug = false;
	$scope.solution = "";
	$scope.schemaPath = "";
	$scope.datasourceName="";
	$scope.xmlaEnabledFlag=true;
	$scope.checkboxModel = {
		manifest: false,
		deletePerm: true,
		keep: true,
		delete: true,
		publishCube: false,
		debug: false
	};

	$scope.getListView = function (dataResultSync) {
		$scope.loading = true;
		
		
		$scope.api = "fs";
		if ($scope.switchFlag) $scope.api = "jcr";

		pentahoService.getList($scope).success(function (data) {
			if (data.error == 'false') {
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
				$scope.exclude = data.exclude;
				$scope.create = data.create;
				$scope.update = data.update;
				$scope.delete = data.delete;
				$scope.preserve = data.preserve;

				$scope.excludeSize = (data.exclude == undefined) ? 0 : data.exclude.length;
				$scope.createSize = (data.create == undefined) ? 0 : data.create.length;
				$scope.updateSize = (data.update == undefined) ? 0 : data.update.length;
				$scope.deleteSize = (data.delete == undefined) ? 0 : data.delete.length;
				$scope.preserveSize = (data.preserve == undefined) ? 0 : data.preserve.length;
				$scope.showFlag = true;
				
				if($scope.haveFile()){
					$scope.hideSync = false;
				}else{
					setMessageAlert("There is no file change for synchronization.", "false");
					$scope.loading = false;
					$scope.hideSync = true;
				}
				
			}
			else {
				$scope.showFlag = false;
				setMessageAlert(data.message + ": " + data.error_message, data.error);
			}
		}).error(function (data, status) {
			setMessageAlert("Aconteceu um problema: " + data, "true");
		}).finally(function () {
			$scope.loading = false;
			if (dataResultSync != undefined) {
				setMessageAlert(dataResultSync.message, dataResultSync.error);
			}
		});
	};
	
	$scope.haveFile= function(){
		return $scope.createSize > 0 || $scope.updateSize>0 || $scope.deleteSize>0 ? true :false;
	}

	$scope.sync = function () {
		$scope.loading = true;
		$scope.api = "fs";
		if ($scope.switchFlag) $scope.api = "jcr";
		
		if($scope.haveFile()){
			pentahoService.sync($scope).success(function (data) {
				if (data.error == 'false') {
					$scope.getListView(data);
					if($scope.checkboxModel.publishCube){
						$scope.publishCube();
					}
					
				}
				else {
					setMessageAlert(data.message + ": " + data.error_message, "true");
				}
	
			}).error(function (data, status) {
				setMessageAlert("Aconteceu um problema: " + data, "true");
	
			}).finally(function () {
				// $scope.loading = false;
				
			});
		}else {
			
//			setMessageAlert("There is no file change for synchronization.", "false");
			if($scope.checkboxModel.publishCube && $scope.api=="jcr" ){
				$scope.publishCube();
			}else{
				$scope.loading = false;
			}
			
		}
	};
	
	$scope.publishCube = function(){
		$scope.loading = true;
		
		pentahoService.publishCube($scope).success(function (data) {
			if (data.error == 'false') {
				setMessageAlert(data.message, "false");
			}
			else {
				setMessageAlert(data.message + ": " + data.error_message, "true");
			}

		}).error(function (data, status) {
			setMessageAlert("Aconteceu um problema: " + data, "true");

		}).finally(function () {
			$scope.loading = false;
			
		});
	}

	$scope.collapse = {};
	$scope.collapse.cr = "collapse";
	$scope.collapse.de = "collapse";
	$scope.collapse.up = "collapse";
	$scope.collapse.ex = "collapse";
	$scope.collapse.pr = "collapse";

	$scope.setCollapse = function (id) {
		($scope.collapse[id].length == 0) ? ($scope.collapse[id] = "collapse") : ($scope.collapse[id] = "");
	};

	var setMessageAlert = function (msg, error) {
		$("#message-box").removeClass();
		if (error == "true")
			$("#message-box").addClass("alert alert-danger");
		else
			$("#message-box").addClass("alert alert-success");
		$("#message-alert").html(msg);
		$("#message-box").alert();
		$("#message-box").fadeTo(5000, 500).slideUp(500, function () {
			$("#message-box").hide();
		});
	};

});