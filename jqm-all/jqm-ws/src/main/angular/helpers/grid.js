// Templates for grid cells
import angular from 'angular';
import { uiGrid } from 'angular-ui-grid';
import uigrid from 'angular-ui-grid/ui-grid.js';

const xxx = angular.module('jqm.editors', ['ui.grid']);

xxx.constant('jqmCellTemplateBoolean',
    '<div class="ui-grid-cell-contents"><span class="far {{ row.entity[col.field] ? \'fa-check-square\' : \'fa-square\' }}"></span></div>');
xxx.constant('jqmCellEditorTemplateBoolean',
    '<div><input jqm-grid-boolean-editorX class="ui-grid-cell-contents" type="checkbox" ng-model="MODEL_COL_FIELD"/></div>');

xxx.directive('jqmGridBooleanEditor', ['uiGridEditConstants', '$timeout', function (uiGridEditConstants, $timeout)
{
    return {
        scope: true,
        require: ['?^uiGrid', '?^uiGridRenderContainer', 'ngModel'],
        compile: function ()
        {
            return {
                pre: function ($scope, $elm, $attrs)
                {
                },

                post: function ($scope, $elm, $attrs, controllers)
                {
                    var uiGridCtrl, renderContainerCtrl, ngModel;
                    if (controllers[0])
                    {
                        uiGridCtrl = controllers[0];
                    }
                    if (controllers[1])
                    {
                        renderContainerCtrl = controllers[1];
                    }
                    if (controllers[2])
                    {
                        ngModel = controllers[2];
                    }
                    var grid = uiGridCtrl.grid;

                    $elm.on('blur', function (evt)
                    {
                        $scope.$emit(uiGridEditConstants.events.END_CELL_EDIT);
                        return true;
                    });

                    // Just change the boolean on edit start
                    $scope.$on(uiGridEditConstants.events.BEGIN_CELL_EDIT, function (evt, triggerEvent)
                    {
                        //jQuery($elm[0]).focus(); // TODO: reactivate without jquery
						/*$timeout(function() {
							$($elm[0]).click();
						});*/ // changing the value on "enter edit mode" event is actually really disturbing for the user. Disabled for now.
                    });
                }
            };
        }
    };
}]);


export const jqmHelperModule = xxx.name;
