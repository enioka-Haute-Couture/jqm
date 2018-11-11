'use strict';

import $ from 'jquery'; // needed by bootstrap js.
import 'bootstrap-datepicker/dist/js/bootstrap-datepicker';
import 'bootstrap-datepicker/dist/css/bootstrap-datepicker.min.css'

class DatePickerController
{
    constructor($scope)
    {
        this.$scope = $scope;
        this.id = ("dp-" + Math.random()).replace('.', '');

        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.dp = $("#" + this.id);

        var $ctrl = this;
        this.dp.datepicker().on('changeDate', function (e)
        {
            e.stopPropagation();
            if ((!$ctrl.ngModel && !$ctrl.dp.datepicker('getDate')) || ($ctrl.ngModel && $ctrl.dp.datepicker('getDate').getTime() === $ctrl.ngModel.getTime()))
            {
                return; // Already at the right value - the event likely comes from the $doCheck.
            }

            // Give the date object to parent through angular binding.
            $ctrl.ngModel = $ctrl.dp.datepicker('getDate');
            $ctrl.$scope.$apply();

            // Call optional callback.
            if ($ctrl.ngChange)
            {
                $ctrl.ngChange(e);
            }
        });
    }

    $onDestroy()
    {
        // Remove handlers - avoids leaks.
        this.dp.datepicker().off('changeDate');
    }

    $doCheck()
    {
        if (this.ngModel && this.dp && this.dp.datepicker('getDate').getTime() !== this.ngModel.getTime())
        {
            this.dp.datepicker('setDate', this.ngModel);
        }
        else if (this.dp && !this.ngModel && this.dp.datepicker('getDate'))
        {
            this.dp.datepicker('clearDates');
        }
    }
}
DatePickerController.$inject = ['$scope',];

export const datepickerComponent = {
    controller: DatePickerController,
    template: ' ' +
        '<div id={{$ctrl.id}} class="input-group date" data-provide="datepicker" data-date-autoclose="true" data-date-week-start="1" data-date-calendar-weeks="true" data-date-clear-btn="true" data-date-max-view-mode="years" data-date-title="{{$ctrl.ngTitle}}" data-date-format="dd/mm/yyyy" data-date-today-btn="true" data-date-today-highlight="true"> ' +
        '   <input type="text" class="form-control">' +
        '   <div class="input-group-addon input-group-append">' +
        '      <span class="input-group-text fas fa-th"></span>' +
        '   </div>' +
        '</div>',
    bindings: {
        'ngTitle': '@',
        'ngModel': '=',
        'ngChange': '<?'
    }
};
