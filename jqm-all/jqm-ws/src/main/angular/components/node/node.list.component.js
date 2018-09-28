'use strict';

import template from './node.list.template.html';
import { createGlobalFilter } from '../../helpers/filters';


class NodeListCtrl
{
	constructor($uibModal, µNodeDto, jqmCellTemplateBoolean, jqmCellEditorTemplateBoolean, uiGridConstants)
	{
		var ctx = this;
		this.µNodeDto = µNodeDto;
		this.$uibModal = $uibModal;

		this.items = [];
		this.selected = [];

		ctx.gridOptions = {
			data: '$ctrl.items',

			enableSelectAll: false,
			enableRowSelection: true,
			enableRowHeaderSelection: false,
			enableFullRowSelection: true,
			enableFooterTotalSelected: false,
			multiSelect: false,
			enableSelectionBatchEvent: false,
			noUnselect: true,

			enableColumnMenus: false,
			enableCellEditOnFocus: true,
			virtualizationThreshold: 20,
			enableHorizontalScrollbar: 0,

			onRegisterApi: function (gridApi)
			{
				ctx.gridApi = gridApi;
				gridApi.selection.on.rowSelectionChanged(null, function (rows)
				{
					ctx.selected = gridApi.selection.getSelectedRows();
				});

				gridApi.cellNav.on.navigate(null, function (newRowCol, oldRowCol)
				{
					if (newRowCol !== oldRowCol)
					{
						gridApi.selection.selectRow(newRowCol.row.entity);
					}
				});

				ctx.gridApi.grid.registerRowsProcessor(createGlobalFilter(ctx, ['name', 'dns']), 200);
			},

			columnDefs: [
				{
					field: 'name',
					displayName: 'Name',
					width: '**',
					cellTemplate: '<div ng-class="{\'bg-success\': row.entity[\'reportsRunning\'] === true, \'bg-danger\': row.entity[\'reportsRunning\'] === false}"> \
													<div class="ui-grid-cell-contents">{{row.entity[col.field]}}</div></div>',
					sort: {
						direction: uiGridConstants.DESC,
						priority: 0
					},
				}, {
					field: 'dns',
					displayName: 'DNS to bind to',
					width: '**',
				}, {
					field: 'port',
					displayName: 'HTTP port',
					width: '*',
				}, {
					field: 'outputDirectory',
					displayName: 'File produced storage',
					width: '***',
				}, {
					field: 'jobRepoDirectory',
					displayName: 'Directory containing jars',
					width: '***',
				}, {
					field: 'tmpDirectory',
					displayName: 'Temporary directory',
					width: '**',
				}, {
					field: 'rootLogLevel',
					displayName: 'Log level',
				}, {
					field: 'jmxRegistryPort',
					displayName: 'jmxRegistryPort',
				}, {
					field: 'jmxServerPort',
					displayName: 'jmxServerPort',
				}, {
					field: 'enabled',
					displayName: 'Enabled',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: '*',
				}, {
					field: 'loapApiSimple',
					displayName: 'Simple API',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: '*',
				}, {
					field: 'loadApiClient',
					displayName: 'Client API',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: '*',
				}, {
					field: 'loadApiAdmin',
					displayName: 'Admin API',
					cellTemplate: jqmCellTemplateBoolean,
					editableCellTemplate: jqmCellEditorTemplateBoolean,
					width: '*',
				}, {
					field: 'id',
					enableCellEdit: false,
					displayName: '',
					cellTemplate: '<div class="ngCellText"><a ng-click="showlog(row.entity.name)">log</a></div>',
					width: '*',
				}]
		};

		this.helpcontent = {
			title: "Nodes are instances of the JQM engine that actually run batch job instances. Basically, they are Unix init.d entries, Windows services or running containers.",
			paragraphs: ["On this page, one may change the characteristics of nodes.",
				"Nodes can only be created through the command line <code>jqm.(sh|ps1) createnode [ nodename ]</code> . Only nodes switched off for more than 10 minutes can be removed.",
				"Changing fields marked with <strong>*</strong> while the node is running requires the node to be restarted for the change to be taken into account. Changes to other fields are automatically applied asynchronously (default is at most after one minute).",
			],
			columns: {
				"* Name": "The name of the node inside the JQM cluster. It has no link whatsoever to hostname, DNS names and whatnot. It is simply the name given as a parameter to the node when starting. It is unique throughout the cluster. Default is server hostname in Windows, user name in Unix.",
				"DNS to bind to": "The web APIs will bind on all interfaces that answer to a reverse DNS call of this name. Default is localhost, i.e. local-only binding.",
				"HTTP port": "The web APIs will bind on this port. Default is a random free port.",
				"* File produced storage": "Should batch jobs produce files, they would be stored in sub-directories of this directory. Absolute path strongly recommended, relative path are relative to JQM install directory.",
				"* Jar directory": "The root directory containing all the jobs (payload jars). Absolute path strongly recommended, relative path are relative to JQM install directory.",
				"Log level": "Verbosity of the main log file. Valid values are TRACE, DEBUG, INFO, WARN, ERROR, FATAL. See full documentation for the signification of these levels. In case of erroneous value, default value INFO is assumed.",
				"* JMX registry TCP port": "If 0, remote JMX is disabled. Default is 0.",
				"* JMX server TCP port": "If 0, remote JMX is disabled. Default is 0.",
				"* JMX server TCP port": "If 0, remote JMX is disabled. Default is 0.",
				"Simple API": "If ticked, the simple web API will start. This API governs script interactions (execution request through wget & co, etc.) and file retrieval (logs, files created by batch jobs executions)",
				"Client API": "If ticked, the client web API will start. This API exposes the full JqmClient API - see full documentation.",
				"Admin API": "If ticked, the administration web API will start. This API is only used by this web administration console and is an internal JQM API, not a public one. Disabling it disables this web console.",
			},
			notes: ["Disabling the three web APIs will fully disable the internal web server of a node, which in turn is ahuge memory gain. But please note that the simple API is fundamental to file operations.",
				"Node colour green means the node has reported it was running to the database less than parameter 'internalPollingPeriodMs' ago. Results may be delayed by up to 60s (due to the cache of the web service)."]
		};

		this.refresh();
	}


	refresh()
	{
		this.selected.length = 0;
		this.items = this.µNodeDto.query();
	};

	// Only remove from list - save() will sync the list with
	// the server so no need to delete it from server now
	remove()
	{
		var q = null;
		for (var i = 0; i < this.selected.length; i++)
		{
			q = this.selected[i];
			this.items.splice(this.items.indexOf(q), 1);
		}
		this.selected.length = 0;
	};


	save()
	{
		// Save and refresh the table - ID may have been
		// generated by the server.
		this.µNodeDto.saveAll({}, this.items, this.refresh);
	};

	showlog(nodeName)
	{
		$uibModal.open({
			templateUrl: './template/file_reader.html',
			controller: 'fileReader',
			size: 'lg',

			resolve: {
				url: function ()
				{
					return "ws/admin/node/" + nodeName + "/log?latest=" + 200;
				}
			},
		});
	};

	stop()
	{
		var q = null;
		for (var i = 0; i < this.selected.length; i++)
		{
			q = this.selected[i];
			q.stop = true;
			q.$save();
		}
	};
}
NodeListCtrl.$inject = ['$uibModal', 'µNodeDto', 'jqmCellTemplateBoolean', 'jqmCellEditorTemplateBoolean', 'uiGridConstants'];

export const nodeListComponent = {
	controller: NodeListCtrl,
	template: template,
	bindings: {
		someInput: '<',
		someOutput: '&'
	}
};
