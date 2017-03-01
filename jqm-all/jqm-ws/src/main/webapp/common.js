// Templates for grid cells

var module = angular.module('jqm.editors', [ 'ui.grid' ]);

module.constant('jqmCellTemplateBoolean',
		'<div class="ui-grid-cell-contents"><span class="glyphicon {{ row.entity[col.field] ? \'glyphicon-ok\' : \'glyphicon-none\' }}"></span></div>');
module.constant('jqmCellEditorTemplateBoolean',
		'<div><input jqm-grid-boolean-editor class="ui-grid-cell-contents" type="checkbox" ng-model="MODEL_COL_FIELD"/></div>');

module.directive('jqmGridBooleanEditor', [ 'uiGridConstants', 'uiGridEditConstants', '$timeout', function(uiGridConstants, uiGridEditConstants, $timeout) {
	return {
		scope : true,
		require : [ '?^uiGrid', '?^uiGridRenderContainer', 'ngModel' ],
		compile : function() {
			return {
				pre : function($scope, $elm, $attrs) {
				},

				post : function($scope, $elm, $attrs, controllers) {
					var uiGridCtrl, renderContainerCtrl, ngModel;
					if (controllers[0]) {
						uiGridCtrl = controllers[0];
					}
					if (controllers[1]) {
						renderContainerCtrl = controllers[1];
					}
					if (controllers[2]) {
						ngModel = controllers[2];
					}
					var grid = uiGridCtrl.grid;

					$elm.on('blur', function(evt) {
						$scope.$emit(uiGridEditConstants.events.END_CELL_EDIT);
						return true;
					});

					// Just change the boolean on edit start
					$scope.$on(uiGridEditConstants.events.BEGIN_CELL_EDIT, function(evt, triggerEvent) {
						$($elm[0]).focus();
						/*$timeout(function() {
							$($elm[0]).click();
						});*/ // changing the value on "enter edit mode" event is actually really disturbing for the user. Disabled for now.
					});
				}
			};
		}
	};
} ]);

function createGlobalFilter($scope, columns) {
	return function(renderableRows) {
		if (!$scope.filterValue) {
			return renderableRows;
		}
		var matcher = new RegExp($scope.filterValue.toLowerCase());
		renderableRows.forEach(function(row) {
			var match = false;
			columns.forEach(function(field) {
				if (row.entity[field] && row.entity[field].toLowerCase().match(matcher)) {
					match = true;
				}
			});
			if (!match) {
				row.visible = false;
			}
		});
		return renderableRows;
	};
}