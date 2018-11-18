'use strict'

import * as SVG from 'svg.js';

class GanttController
{
    constructor(dateFilter)
    {
        this.dateFilter = dateFilter;

        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.draw = SVG('drawing').size(window.innerWidth - 50, 500);
    }

    $onChanges(changesObj)
    {
        if (changesObj.jqmAlgo)
        {
            this.drawData = this["drawData" + this.jqmAlgo].bind(this);
            // Do not redraw, as an algo change goes with a click.
        }

        if (changesObj.jqmData && this.jqmData && this.draw && this.drawData)
        {
            this.drawData();
        }
    }

    drawData1()
    {
        console.time("drawData1");
        this.draw.clear();
        if (!this.jqmData || this.jqmData.length == 0)
        {
            console.timeEnd("drawData1");
            return;
        }

        // No need to copy data anymore - data is just for us.
        var data = this.jqmData;

        // Data is sorted on the DB side by beganRunningDate descending.

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
                queue.lines.unshift(freeLine);
                lineCount++;
            }
            freeLine.push(ji);

            // Also check max/min.
            if (!maxEnd || maxEnd < ji.end)
            {
                maxEnd = ji.end;
            }
        }
        minStart = data[0].start;

        // Go to drawing.
        var PX_PER_MS = (window.innerWidth - 50) / (maxEnd.getTime() - minStart.getTime());
        var PX_PER_LINE = 500 / lineCount;
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
                var y = (lineIdx + 0.5) * PX_PER_LINE;

                // Draw each segment inside the line
                for (var ji of line)
                {
                    x1 = (ji.start.getTime() - minStart.getTime()) * PX_PER_MS;
                    x2 = (ji.end.getTime() - minStart.getTime()) * PX_PER_MS;

                    //this.draw.line(x1, y,x2, y).stroke({ width: 2, color: color });
                    //this.draw.line(x2, y-5,x2, y+5).stroke({ width: 2, color: color });

                    this.draw.polyline([x1, y - 5, x1, y + 5, x1, y, x2, y]).fill('none').stroke({ width: 2, color: color });
                }

                lineIdx++;
                jiInsideQueue += line.length;
            }

            // Draw queue legend
            this.draw.text(queueName + " - " + jiInsideQueue).move(20, (lineIdx - 0.4) * PX_PER_LINE).attr({ fill: color });

            queueIdx++;
        }

        // Global date legend
        this.draw.text(this.dateFilter(minStart, 'dd/MM HH:mm:ss'));
        this.draw.text(this.dateFilter(maxEnd, 'dd/MM HH:mm:ss')).move(window.innerWidth - 150);

        console.timeEnd("drawData1");
    }

    drawData0()
    {
        console.time("drawData0");
        this.draw.clear();
        if (!this.jqmData || this.jqmData.length == 0)
        {
            return;
        }

        var width = window.innerWidth - 50;
        var height = 500;

        var startJobs = []; // Shallow copy.
        var endJobs = []; // Shallow copy.
        var queues = new Map();
        var queue = null;
        var job = null;

        for (var ji of this.jqmData)
        {
            if (ji.beganRunningDate)
            {
                ji.start = new Date(ji.beganRunningDate);
                startJobs.push(ji);
                if (!queues[ji.queue.name])
                {
                    queues[ji.queue.name] = { name: ji.queue.name, count: 0, max: 0, slots: [], color: null, offset: 0 };
                }
            }
            if (ji.endDate)
            {
                ji.end = new Date(ji.endDate);
                endJobs.push(ji);
            }
        }

        // Sort ensure stability by sorting on job.id in case of simultaneity
        // Sort jobs by start dates
        startJobs.sort(function (a, b)
        {
            if ((a.start != null) && (b.start != null))
            {
                if (a.start == b.start)
                {
                    return a.id - b.id;
                } else
                {
                    return (a.start - b.start);
                }
            }
            if (a.start != null)
            {
                return 3;
            }
            if (b.start != null)
            {
                return -3;
            }
        }
        ); // sort by startup.

        // Sort jobs by end dates
        endJobs.sort(function (a, b)
        {
            if ((a.end != null) && (b.end != null))
            {
                if (a.end == b.end)
                {
                    return a.id - b.id;
                } else
                {
                    return (a.end - b.end);
                }
            }
            if (a.start != null)
            {
                return 3;
            }
            if (b.start != null)
            {
                return -3;
            }
        }
        ); // sort by end.

        var start = null;
        var end = null;

        if (startJobs.length >= 1)
        {
            start = startJobs[0];
        }
        if (endJobs.length >= 1)
        {
            end = endJobs[0];
        }

        // Could be further mutualized and simplified
        // Just a small function to assign a slot to any starting job
        // and keeping track of slot deallocation when jobs end
        function pushQueue(job, queue, date, delta, queues)
        {
            var count = queues[queue].count + delta;
            queues[queue].count = count;
            if (delta > 0)
            {
                if (queues[queue].max < count)
                {
                    queues[queue].slots[count - 1] = false;
                    queues[queue].max = count;
                }
                for (var i = 0; i < queues[queue].max; i++)
                {
                    if (!queues[queue].slots[i])
                    {
                        queues[queue].slots[i] = true;
                        job.slot = i;
                        break;
                    }
                }
            } else
            {
                queues[queue].slots[job.slot] = false;
            }
        }

        // Compute slot of each job and assign it in slot field of jobs (ji)
        // Based on two sorted lists scanned concurrently : ordered list of started jobs
        // and ordered list of ended jobs
        var startIndex = 0;
        var endIndex = 0;
        while ((start != null) || (end != null))
        {
            if (start != null)
            {
                if (end != null)
                {
                    if (start.start.getTime() < end.end.getTime())
                    {
                        pushQueue(start, start.queue.name, start.start, 1, queues);
                        if (startJobs.length > (startIndex + 1))
                        {
                            start = startJobs[startIndex + 1];
                            startIndex++;
                        } else
                        {
                            start = null;
                        }
                    } else
                    {
                        pushQueue(end, end.queue.name, end.end, -1, queues);
                        if (endJobs.length > (endIndex + 1))
                        {
                            end = endJobs[endIndex + 1];
                            endIndex++;
                        } else
                        {
                            end = null;
                        }
                    }
                } else
                {
                    pushQueue(start, start.queue.name, start.start, 1, queues);
                    if (startJobs.length > (startIndex + 1))
                    {
                        start = startJobs[startIndex + 1];
                        startIndex++;
                    } else
                    {
                        start = null;
                    }
                }
            } else
            {
                if (end != null)
                {
                    pushQueue(end, end.queue.name, end.end, -1, queues);
                    if (endJobs.length > (endIndex + 1))
                    {
                        end = endJobs[endIndex + 1];
                        endIndex++;
                    } else
                    {
                        end = null;
                    }
                }
            }
        }

        var count = 0;
        var colorsCount = 0;

        // Sets queue colors and offsets
        for (var qname in queues)
        {
            queue = queues[qname];
            count += queue.max;
            queue.offset = count;
            queue.color = colorsCount;
            colorsCount++;
        }

        // Compute min & max times for display scale
        var min = -1;
        var minStart = null;
        var maxEnd = null;
        var max = -1;
        for (job of startJobs)
        {
            if (job.start != null)
            {
                if (min == -1)
                {
                    min = job.start.getTime();
                    minStart = job.start;
                } else
                {
                    if (min > job.start.getTime())
                    {
                        min = job.start.getTime();
                        minStart = job.start;
                    }
                }
                if (max == -1)
                {
                    max = job.start.getTime();
                    maxEnd = job.start;
                } else
                {
                    if (max < job.start.getTime())
                    {
                        max = job.start.getTime();
                        maxEnd = job.start;
                    }
                }
            }
            if (job.end != null)
            {
                if (min == -1)
                {
                    min = job.end.getTime();
                    minStart = job.end;
                } else
                {
                    if (min > job.end.getTime())
                    {
                        min = job.end.getTime();
                        minStart = job.end;
                    }
                }
                if (max == -1)
                {
                    max = job.end.getTime();
                    maxEnd = job.end;
                } else
                {
                    if (max < job.end.getTime())
                    {
                        max = job.end.getTime();
                        maxEnd = job.end;
                    }
                }
            }
        }

        var COLORS = ['#e6194b', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#46f0f0', '#f032e6', '#bcf60c', '#fabebe', '#008080', '#e6beff', '#9a6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075', '#808080', '#ffffff', '#000000'];

        for (qname of queues)
        {
            queue = queues[qname];
            this.draw.text(queue.name).move(50, (queue.offset * height / count - height / (count * 4))).font({ fill: COLORS[queue.color], size: 15 });
        }

        for (job of startJobs)
        {
            var startTime = min;
            var endTime = max;
            if (job.start != null)
            {
                startTime = job.start.getTime();
            }
            if (job.end != null)
            {
                endTime = job.end.getTime();
            }
            var x1, y, x2;
            y = (queues[job.queue.name].offset - job.slot) * height / count - height / (count * 2);
            x1 = ((startTime - min) * width / (max - min));
            x2 = ((endTime - min) * width / (max - min));

            this.draw.polyline([x1, y - 5, x1, y + 5, x1, y, x2, y]).fill('none').stroke({ width: 2, color: COLORS[queues[job.queue.name].color] });
            //this.draw.text(job.applicationName + job.id).move(x1, y1 - 10).font({ fill: COLORS[queues[job.queue.name].color], size: 8 });
        }

        // Legend
        this.draw.text(this.dateFilter(minStart, 'dd/MM HH:mm:ss'));
        this.draw.text(this.dateFilter(maxEnd, 'dd/MM HH:mm:ss')).move(window.innerWidth - 150);
        console.timeEnd("drawData0");
    }
}
GanttController.$inject = ["dateFilter",];

export const ganttComponent = {
    controller: GanttController,
    template: '<div id="drawing"></div>',
    bindings: {
        'jqmData': '<',
        'jqmAlgo': '<',
    }
};
