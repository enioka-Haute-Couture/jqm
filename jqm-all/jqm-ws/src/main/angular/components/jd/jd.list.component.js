'use strict';

import template from './jd.list.template.html';
import { createGlobalFilter } from '../../helpers/filters';



class JdListCtrl
{
	constructor($interval, µJdDto, µQueueDto, jqmCellTemplateBoolean, jqmCellEditorTemplateBoolean)
	{
		this.jds = null;
		this.selected = [];
		this.queues = [];
		this.gridApi = null;

		this.µJdDto = µJdDto;
		this.µQueueDto = µQueueDto;
		this.$interval = $interval;

		var $ctrl = this;
		this.gridOptions = {
			data: '$ctrl.jds',

			enableSelectAll: false,
			enableRowSelection: true,
			enableRowHeaderSelection: false,
			enableFullRowSelection: true,
			enableFooterTotalSelected: false,
			multiSelect: false,

			enableColumnMenus: false,
			enableCellEditOnFocus: true,
			virtualizationThreshold: 20,
			enableHorizontalScrollbar: 0,

			onRegisterApi: function (gridApi)
			{
				$ctrl.gridApi = gridApi;
				gridApi.selection.on.rowSelectionChanged(null, function (rows)
				{
					$ctrl.selected = gridApi.selection.getSelectedRows();
				});

				gridApi.cellNav.on.navigate(null, function (newRowCol, oldRowCol)
				{
					if (newRowCol !== oldRowCol)
					{
						gridApi.selection.selectRow(newRowCol.row.entity);
					}
				});

				$ctrl.gridApi.grid.registerRowsProcessor(createGlobalFilter($ctrl, ['applicationName', 'description', 'module', 'application', 'javaClassName']), 200);
			},


			columnDefs: [
				{
					field: 'applicationName',
					displayName: 'Name',
					width: '**',
				},
				{
					field: 'description',
					displayName: 'Description',
					width: '***',
				},
				{
					field: 'javaClassName',
					displayName: 'Class to launch',
					width: '**',
				},
				{
					field: 'jarPath',
					displayName: 'Path to the jar (relative to repo)',
					width: '***',
				},
				{
					field: 'canBeRestarted',
					displayName: 'R',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: 28,
				},
				{
					field: 'highlander',
					displayName: 'H',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: 28,
				},
				{
					field: 'queueId',
					displayName: 'Queue',
					//cellTemplate: '<div class="ui-grid-cell-contents"><span ng-cell-text>{{ (row.entity["queueId"] | getByProperty:"id":grid.appScope.queues).name }}</span></div>',
					editableCellTemplate: 'ui-grid/dropdownEditor',
					editDropdownValueLabel: 'name',
					editDropdownOptionsArray: this.queues,
				}, {
					field: 'application',
					displayName: 'Application'
				}, {
					field: 'module',
					displayName: 'Module'
				}, {
					field: 'keyword1',
					displayName: 'Keyword1'
				}, {
					field: 'keyword2',
					displayName: 'Keyword2'
				}, {
					field: 'keyword3',
					displayName: 'Keyword3'
				}, {
					field: 'reasonableRuntimeLimitMinute',
					displayName: 'AlertMn',
					type: 'number',
				},
				{
					field: 'schedules.length',
					displayName: 'S',
					enableCellEdit: false,
					width: 28,
				},
				{
					field: 'enabled',
					displayName: 'E',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: 28,
				},]
		};

		this.helpcontent = {
			title: "A Job Definition is made of all the metadata required to launch a payload: path to the jar file, class to launch, etc.",
			paragraphs: ["On this page, one may change the characteristics of Job Definitions. Changes on this page do not require any node reboot. Changing the payload jars on the server does not require reboots either.",
			],
			columns: {
				"Name": "The name of the job definition. This name is very important, as it is the key used to designate the JD in the different APIs (for example, when submitting an execution request, one will specify by name the batch job to run). However, it can still be changed - internally, JQM uses an ID, not this name - the impact is only on the clients' side.",
				"Description": "A free text description that appears in reports.",
				"Class to launch": "The fully qualified name of the class to run. (it must either have a main function, or implement Runnable, or inherit from JobBase)",
				"Path to the jar": "The relative path to the jar containing the class to run. It is relative to the 'directory containing jars' parameter of the different nodes",
				"R": "Whether it is restartable or not",
				"H": "Tick for Highlander mode. In this mode, there can never be more than one instance of the Job Definition running at the same time, as well as no more than one waiting in any queue.",
				"S": "Count of schedules associated to this job definition.",
				"E": "Whether it is enabled or not. If disabled, will always succeed instantly.",
				"Queue": "The queue which will be used when submitting execution requests (if no specific queue is given at request time)",
				"AlertMn": "A JMX alert will be raised if a job instance takes longer than this to complete. Void by default.",
				"Other fields": "These fields are merely tags that can be used for many uses. They are not used by the engine itself."
			}
		};

		this.refresh();
	}

	newitem()
	{
		var t = new this.µJdDto({
			description: 'what the job does',
			queueId: 1,
			javaClassName: 'com.company.product.ClassName',
			canBeRestarted: true,
			highlander: false,
			jarPath: 'relativepath/to/file.jar',
			enabled: true,
			parameters: [],
			applicationName: "job definition " + (this.jds.length + 1),
		});
		this.jds.push(t);
		this.gridApi.selection.selectRow(t);
		var $ctrl = this;
		this.$interval(function ()
		{
			$ctrl.gridApi.cellNav.scrollToFocus(t, $ctrl.gridOptions.columnDefs[0]);
		}, 0, 1);
	};

	save()
	{
		// Save and refresh the table - ID may have been generated by the server.
		this.µJdDto.saveAll({}, this.jds, this.refresh.bind(this));
	};

	refresh()
	{
		this.selected.length = 0;
		this.jds = this.µJdDto.query();
		// $scope.queues = µQueueDto.query();
		var $ctrl = this;
		this.µQueueDto.query().$promise.then(function (data)
		{
			// Do not simply replace the list - append to it so
			// as to keep the same pointers as those saved in
			// the grid configuration.
			$ctrl.queues.length = 0;
			Array.prototype.push.apply($ctrl.queues, data);
		});
	};

	// Only remove from list - save() will sync the list with
	// the server so no need to delete it from server now
	remove()
	{
		var q = null;
		for (var i = 0; i < this.selected.length; i++)
		{
			q = this.selected[i];
			this.jds.splice(this.jds.indexOf(q), 1);
		}
		this.selected.length = 0;
	};



	/*$scope.clo = function ()
	{
		$uibModal.open({
			templateUrl: './template/jd_clo.html',
			controller: 'jdClo',
			size: 'lg',
			resolve: {
				jd: function ()
				{
					return $scope.selected[0];
				}
			},
		});
	}
	$scope.prms = function ()
	{
		$uibModal.open({
			templateUrl: './template/jd_prms.html',
			controller: 'jdPrms',
			size: 'lg',
			resolve: {
				jd: function ()
				{
					return $scope.selected[0];
				}
			},
		});

	};

	$scope.cron = function ()
	{
		$uibModal.open({
			templateUrl: './template/jd_cron.html',
			controller: 'jdCron',
			size: 'lg',
			resolve: {
				jd: function ()
				{
					return $scope.selected[0];
				},
				queues: function ()
				{
					return $scope.queues;
				}
			},
		});
	}*/
};
JdListCtrl.$inject = ["$interval", "µJdDto", "µQueueDto", "jqmCellTemplateBoolean", "jqmCellEditorTemplateBoolean"];


export const jdListComponent = {
	controller: JdListCtrl,
	template: template,
	bindings: {}
};


/*


jqmApp.controller('jdClo', function ($scope, $uibModalInstance, jd)
{
	$scope.selectedJd = jd;
	$scope.data = {
		specificIsolationContext: $scope.selectedJd.specificIsolationContext,
		hiddenJavaClasses: $scope.selectedJd.hiddenJavaClasses,
		childFirstClassLoader: $scope.selectedJd.childFirstClassLoader,

	};

	$scope.ok = function ()
	{
		$scope.selectedJd.childFirstClassLoader = $scope.data.childFirstClassLoader;
		$scope.selectedJd.hiddenJavaClasses = $scope.data.hiddenJavaClasses;
		$scope.selectedJd.specificIsolationContext = $scope.data.specificIsolationContext;
		$uibModalInstance.close();
	};
});


jqmApp.controller('jdCron', function ($scope, $uibModalInstance, $interval, jd, queues)
{
	$scope.selectedJd = jd;
	$scope.queues = [];
	$scope.selectedSchedule = [];
	$scope.selectedSchedulePrm = [];

	$scope.data = {
		jd: $scope.selectedJd
	}


	$scope.queues.push({ id: null, name: 'none' });
	$.each(queues, function ()
	{
		$scope.queues.push(this);
	});

	$scope.gridOptionsCron = {
		data: 'selectedJd.schedules',

		enableSelectAll: false,
		enableRowSelection: true,
		enableRowHeaderSelection: false,
		enableFullRowSelection: true,
		enableFooterTotalSelected: false,
		multiSelect: false,

		enableColumnMenus: false,
		enableCellEditOnFocus: true,
		virtualizationThreshold: 20,
		enableHorizontalScrollbar: 0,

		onRegisterApi: function (gridApi)
		{
			$scope.gridApiCron = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function (rows)
			{
				$scope.selectedSchedule = gridApi.selection.getSelectedRows();
			});

			gridApi.cellNav.on.navigate($scope, function (newRowCol, oldRowCol)
			{
				if (newRowCol !== oldRowCol)
				{
					gridApi.selection.selectRow(newRowCol.row.entity);
				}
			});
		},


		columnDefs: [
			{
				field: 'cronExpression',
				displayName: 'Cron expression',
				width: '***',
			},
			{
				field: 'queue',
				displayName: 'Queue override',
				cellTemplate: '<div class="ui-grid-cell-contents"><span ng-cell-text>{{ (row.entity["queue"] | getByProperty:"id":grid.appScope.queues).name }}</span></div>',
				editableCellTemplate: 'ui-grid/dropdownEditor',
				editDropdownValueLabel: 'name',
				editDropdownOptionsArray: $scope.queues,
				width: '*',
			},
		]
	};

	$scope.newcron = function ()
	{
		var t = {
			id: null,
			cronExpression: "* * * * *",
			queue: null,
			parameters: [],
		};
		$scope.selectedJd.schedules.push(t);
		$scope.gridApiCron.selection.selectRow(t);
		$interval(function ()
		{
			$scope.gridApiCron.cellNav.scrollToFocus(t, $scope.gridOptionsCron.columnDefs[0]);
		}, 0, 1);
	};

	$scope.removecron = function ()
	{
		var q = null;
		for (var i = 0; i < $scope.selectedSchedule.length; i++)
		{
			q = $scope.selectedSchedule[i];
			$scope.selectedJd.schedules.splice($scope.selectedJd.schedules.indexOf(q), 1);
		}
		$scope.selectedSchedule.length = 0;
	};

	$scope.gridOptionsCronPrm = {
		data: 'selectedSchedule[0].parameters',

		enableSelectAll: false,
		enableRowSelection: true,
		enableRowHeaderSelection: false,
		enableFullRowSelection: true,
		enableFooterTotalSelected: false,
		multiSelect: false,

		enableColumnMenus: false,
		enableCellEditOnFocus: true,
		virtualizationThreshold: 20,
		enableHorizontalScrollbar: 0,

		onRegisterApi: function (gridApi)
		{
			$scope.gridApiCronPrm = gridApi;
			gridApi.selection.on.rowSelectionChanged($scope, function (rows)
			{
				$scope.selectedSchedulePrm = gridApi.selection.getSelectedRows();
			});

			gridApi.cellNav.on.navigate($scope, function (newRowCol, oldRowCol)
			{
				if (newRowCol !== oldRowCol)
				{
					gridApi.selection.selectRow(newRowCol.row.entity);
				}
			});
		},


		columnDefs: [
			{
				field: 'key',
				displayName: 'Key',
				width: '*',
			},
			{
				field: 'value',
				displayName: 'Value',
				width: '*',
			},
		]
	};

	$scope.newcronprm = function ()
	{
		var t = {
			key: "key",
			value: "value",
		};
		$scope.selectedSchedule[0].parameters.push(t);
		$scope.gridApiCronPrm.selection.selectRow(t);
		$interval(function ()
		{
			$scope.gridApiCronPrm.cellNav.scrollToFocus(t, $scope.gridOptionsCron.columnDefs[0]);
		}, 0, 1);
	};

	$scope.removecronprm = function ()
	{
		var q = null;
		for (var i = 0; i < $scope.selectedSchedulePrm.length; i++)
		{
			q = $scope.selectedSchedulePrm[i];
			$scope.selectedSchedule[0].parameters.splice($scope.selectedSchedule[0].parameters.indexOf(q), 1);
		}
		$scope.selectedSchedulePrm.length = 0;
	};

	$scope.ok = function ()
	{
		$uibModalInstance.close();
	};
});
*/