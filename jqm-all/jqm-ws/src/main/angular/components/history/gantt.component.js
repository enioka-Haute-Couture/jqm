'use strict'

import * as SVG from 'svg.js';

class GanttController
{
    constructor()
    {
        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.draw = SVG('drawing').size(window.innerWidth - 50, 500);
        if (this.jqmData && this.jqmData.length > 0)
        {
            this.drawData();
        }
    }

    $onChanges(changesObj)
    {
        if (changesObj.jqmData && this.jqmData && this.draw)
        {
            this.drawData.bind(this)();
        }
    }

    drawData()
    {
        this.draw.clear();
        if (!this.jqmData || this.jqmData.length == 0)
        {
            return;
        }

        var data = this.jqmData.slice(); // Shallow copy.
        data.sort(function (a, b) { return new Date(a.beganRunningDate) - new Date(b.beganRunningDate); }); // sort by startup.

        var queues = new Map();
        var minStart;
        var maxEnd;
        var lineCount = 0;
        for (var ji of data)
        {
            if (!ji.beganRunningDate || !ji.endDate)
            {
                continue;
            }
            ji.start = new Date(ji.beganRunningDate);
            ji.end = new Date(ji.endDate);

            var queue = queues.get(ji.queueName);
            if (!queue)
            {
                queue = new Object();
                queue.details = ji.queue;
                queue.lines = [];

                queues.set(ji.queueName, queue);
            }

            // Find first line free at the start time of the JI.
            var freeLine = null;
            for (var line of queue.lines)
            {
                if (line[line.length - 1].end <= ji.start)
                {
                    freeLine = line;
                    break;
                }
            }
            if (!freeLine)
            {
                freeLine = [];
                queue.lines.push(freeLine);
                lineCount++;
            }
            freeLine.push(ji);

            // Also check max/min.
            if (!minStart || minStart > ji.start)
            {
                minStart = ji.start;
            }
            if (!maxEnd || maxEnd < ji.end)
            {
                maxEnd = ji.end;
            }
        }

        console.info(queues);

        // Go to drawing.
        var PX_PER_MS = (window.innerWidth - 50) / (maxEnd.getTime() - minStart.getTime());
        var PX_PER_LINE = 500 / lineCount;
        var COLORS = ['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080', '#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075', '#808080', '#ffffff', '#000000'];

        var queueIdx = 0;
        var lineIdx = 0;
        for (var [queueName, queue] of queues)
        {
            var color = COLORS[queueIdx];

            // Draw each line
            for (var line of queue.lines)
            {
                // Draw each segemnt inside the line
                for (var ji of line)
                {
                    this.draw.rect((ji.end.getTime() - ji.start.getTime()) * PX_PER_MS, PX_PER_LINE / 2).attr({ fill: color }).move((ji.start.getTime() - minStart.getTime()) * PX_PER_MS, lineIdx * PX_PER_LINE);
                }

                lineIdx++;
            }

            // Draw legend
            this.draw.text(queueName).move(0, (lineIdx - 0.5) * PX_PER_LINE).attr({ fill: color });

            queueIdx++;
        }

        // Legend
        this.draw.text(minStart.toString());
        this.draw.text(maxEnd.toString()).move(window.innerWidth - 300);
    }
}
GanttController.$inject = [];

export const ganttComponent = {
    controller: GanttController,
    template: '<div id="drawing"></div>',
    bindings: {
        'jqmData': '<',
    }
};
