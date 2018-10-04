jqmApp.controller('historyDetail', function ($scope, $http, $uibModal, ji) {
    $scope.ji = ji;
    $scope.dels = [];

    $scope.getdel = function () {
        $http.get("ws/client/ji/" + $scope.ji.id + "/files").success($scope.getdelOk);
    };

    $scope.getdelOk = function (data, status, headers, config) {
        $scope.dels = data;
    };

    $scope.showlog = function (url) {
        $uibModal.open({
            templateUrl: './template/file_reader.html',
            controller: 'fileReader',
            size: 'lg',

            resolve: {
                url: function () {
                    return url;
                },
            },
        });
    };

    $scope.getdel();
});

jqmApp.controller('jiNew', function ($scope, µUserJdDto, $uibModalInstance, $http) {
    $scope.jds = µUserJdDto.query();
    $scope.selectedJd = null;

    $scope.data = {
        selectedJd: null,
        newKey: null,
        newValue: null
    };

    $scope.request = {
        user: 'webuser',
        sessionID: 0,
        parameters: [],
    };

    $scope.addPrm = function () {
        var np = {};
        np.key = $scope.data.newKey;
        np.value = $scope.data.newValue;
        $scope.request.parameters.push(np);
    };

    $scope.postOk = function () {
        $uibModalInstance.close();
    };

    $scope.ok = function () {
        $scope.request.applicationName = $scope.selectedJd.applicationName;
        $http.post("ws/client/ji", $scope.request).success($scope.postOk);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
});
