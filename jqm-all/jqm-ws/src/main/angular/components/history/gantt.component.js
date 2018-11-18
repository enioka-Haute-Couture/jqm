'use strict'

import * as SVG from 'svg.js';

class GanttController
{
    constructor(dateFilter, $element)
    {
        this.dateFilter = dateFilter;
        this.drawData = this.drawData1.bind(this);
        this.$element = $element;

        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.draw = SVG('drawing').size(window.innerWidth - 50, 500);
    }

    $onChanges(changesObj)
    {
        if (changesObj.jqmData && this.jqmData && this.draw && this.drawData)
        {
            this.drawData();
        }
    }

    drawData1()
    {
        console.time("drawData1");

        // Reset drawing area.
        this.draw.clear();
        var width = this.$element.parent()[0].clientWidth - 50;
        var height = this.$element.parent()[0].clientHeight - 50;
        this.draw.width(width);
        this.draw.height(height);

        if (height < 100 || width < 100)
        {
            // Just give up.
            console.error("area too small for graph");
            console.timeEnd("drawData1");
            return;
        }

        // Check data
        if (!this.jqmData || this.jqmData.length == 0)
        {
            console.timeEnd("drawData1");
            return;
        }
        // Data is sorted on the DB side by beganRunningDate descending.
        // No need to copy data anymore - data is just for us.
        var data = this.jqmData;

        var queues = new Map();
        var minStart;
        var maxEnd = new Date(data[0].endDate);
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
                queue.lines.unshift(freeLine);
                lineCount++;
            }
            freeLine.push(ji);

            // Also check max/min.
            if (maxEnd < ji.end)
            {
                maxEnd = ji.end;
            }
        }

        for (var ji of data)
        {
            if (ji.start)
            {
                minStart = ji.start;
                break;
            }
        }

        if (!minStart || !maxEnd)
        {
            // No data! (all JI must be SUBMITTED or likewise)
            console.timeEnd("drawData1");
            return;
        }

        // Go to drawing.
        var Y_SHIFT = 30;
        var X_SHIFT = 20;
        var PX_PER_MS = (width - 2 * X_SHIFT) / (maxEnd.getTime() - minStart.getTime());
        var PX_PER_LINE = (height - Y_SHIFT) / (lineCount + queues.size); // Keep a line for queue legend
        var COLORS = ['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080', '#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075', '#808080', '#ffffff', '#000000'];

        var queueIdx = 0;
        var lineIdx = 0;
        var x1 = 0, x2 = 0;

        for (var [queueName, queue] of queues)
        {
            var color = COLORS[queueIdx];
            var jiInsideQueue = 0;

            // Draw each line
            for (var line of queue.lines)
            {
                var y = (lineIdx + 0.5) * PX_PER_LINE + Y_SHIFT;

                // Draw each segment inside the line
                for (var ji of line)
                {
                    x1 = (ji.start.getTime() - minStart.getTime()) * PX_PER_MS + X_SHIFT;
                    x2 = (ji.end.getTime() - minStart.getTime()) * PX_PER_MS + X_SHIFT;

                    //this.draw.line(x1, y,x2, y).stroke({ width: 2, color: color });
                    //this.draw.line(x2, y-5,x2, y+5).stroke({ width: 2, color: color });

                    this.draw.polyline([x1, y - 5, x1, y + 5, x1, y, x2, y]).fill('none').stroke({ width: 2, color: color });
                }

                lineIdx++;
                jiInsideQueue += line.length;
            }

            // Draw queue legend
            this.draw.text(queueName + " - " + jiInsideQueue).move(60, (lineIdx + 0.4) * PX_PER_LINE + Y_SHIFT).font({ size: (PX_PER_LINE - 1) + "px" }).attr({ fill: color });
            lineIdx++; // legend has its own line.

            queueIdx++;
        }

        // Global date legend
        this.draw.text(this.dateFilter(minStart, 'dd/MM HH:mm:ss')).move(X_SHIFT);
        this.draw.text(this.dateFilter(maxEnd, 'dd/MM HH:mm:ss')).move(window.innerWidth - 150);

        console.timeEnd("drawData1");
    }
}
GanttController.$inject = ["dateFilter", '$element',];

export const ganttComponent = {
    controller: GanttController,
    template: '<div id="drawing"></div>',
    bindings: {
        'jqmData': '<',
    }
};
