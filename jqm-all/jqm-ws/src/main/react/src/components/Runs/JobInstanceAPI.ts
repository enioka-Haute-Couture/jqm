import { MUISortOptions } from "mui-datatables";
import { useCallback, useState } from "react";
import { JobInstance } from "./JobInstance";
import { JobLaunchParameters } from "./JobLaunchParameters";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import useQueueAPI from "../Queues/QueueAPI";
import { useRunsPagination } from "../../utils/RunsPaginationProvider";

const API_URL = "/client/ji";

interface JobInstanceFilters {
    user?: string;
    jobInstanceId?: string;
    nodeName?: string;
    enqueuedAfter?: Date;
    enqueuedBefore?: Date;
    parentId?: string;
    queueId?: number;
    sessionId?: string;
    applicationName?: string[];
    statuses: string[];
}

export const useJobInstanceAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const {
        page,
        rowsPerPage,
        sortOrder,
        filterList,
        queryLiveInstances,
        setPage,
        setRowsPerPage,
        setSortOrder,
        setFilterList,
        setQueryLiveInstances,
    } = useRunsPagination();

    const [jobInstances, setJobInstances] = useState<JobInstance[] | null>();
    const [count, setCount] = useState<number>(0);

    const { queues, fetchQueues } = useQueueAPI();

    const fetchJobInstances = useCallback(
        async (
            newPage: number,
            newRowsPerPage: number,
            newSortOrder: MUISortOptions,
            newQueryLiveInstances: boolean,
            newFilterList: string[][]
        ) => {
            if (newQueryLiveInstances !== queryLiveInstances) {
                setJobInstances(null);
            }
            setPage(newPage);
            setRowsPerPage(newRowsPerPage);
            setSortOrder(newSortOrder);
            setQueryLiveInstances(newQueryLiveInstances);

            let filterQuery: JobInstanceFilters = { statuses: [] };

            setFilterList(newFilterList);

            if (
                newFilterList[0]?.length > 0 &&
                !isNaN(parseInt(newFilterList[0][0]))
            ) {
                filterQuery.jobInstanceId = newFilterList[0][0];
            }
            if (newFilterList[1]?.length > 0) {
                filterQuery.applicationName = [newFilterList[1][0]];
            }
            if (newFilterList[2]?.length > 0) {
                filterQuery.queueId = queues?.find(
                    (queue) => queue.name === newFilterList[2][0]
                )?.id;
            }
            if (newFilterList[3]?.length > 0) {
                filterQuery.statuses = [newFilterList[3][0]];
            }
            if (newFilterList[4]?.length > 0) {
                filterQuery.enqueuedAfter = new Date(newFilterList[4][0]);
                filterQuery.enqueuedBefore = new Date(newFilterList[4][1]);
            }
            if (newFilterList[8]?.length > 0) {
                filterQuery.user = newFilterList[8][0];
            }
            if (
                newFilterList[9]?.length > 0 &&
                !isNaN(parseInt(newFilterList[9][0]))
            ) {
                filterQuery.parentId = newFilterList[9][0];
            }
            if (newFilterList[11]?.length > 0) {
                filterQuery.sessionId = newFilterList[11][0];
            }

            var sortColumnName = newSortOrder.name.toUpperCase();
            // For some reason field names and filter names are not the same, so do mapping here
            if (sortColumnName === "STATE") {
                sortColumnName = "STATUS";
            }
            if (sortColumnName === "ENQUEUEDATE") {
                sortColumnName = "DATEENQUEUE";
            }
            if (sortColumnName === "BEGANRUNNINGDATE") {
                sortColumnName = "DATEEXECUTION";
            }
            if (sortColumnName === "ENDDATE") {
                sortColumnName = "DATEEND";
            }
            if (sortColumnName === "USER") {
                sortColumnName = "USERNAME";
            }
            if (sortColumnName === "PARENT") {
                sortColumnName = "PARENTID";
            }
            const sortBy = [
                {
                    col: sortColumnName,
                    order:
                        newSortOrder.direction === "asc"
                            ? "ASCENDING"
                            : "DESCENDING",
                },
            ];

            let request = {
                ...filterQuery,
                firstRow: newRowsPerPage * newPage,
                pageSize: newRowsPerPage,
                queryLiveInstances: newQueryLiveInstances,
                queryHistoryInstances: !newQueryLiveInstances,
                sortby: sortBy,
            };

            const queryDate = new Date().getTime();
            return APIService.post(`${API_URL}/query/`, request)
                .then((response) => {
                    if (newQueryLiveInstances) {
                        // For live instances end date is not set, so set it to the query date to compute duration
                        response["instances"]?.forEach((instance: any) => {
                            if (instance.beganRunningDate) {
                                instance.endDate = queryDate;
                            }
                        });
                    }
                    setJobInstances(response["instances"]);
                    setCount(response["resultSize"]);
                })
                .catch(displayError);
        },
        [
            queryLiveInstances,
            setPage,
            setRowsPerPage,
            setSortOrder,
            setQueryLiveInstances,
            setFilterList,
            displayError,
            queues,
        ]
    );

    const killJob = useCallback(
        async (jobId: number) => {
            return APIService.post(`${API_URL}/killed/${jobId}`, {})
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(`Successfully killed job ${jobId}`);
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const pauseJob = useCallback(
        async (jobId: number) => {
            return APIService.post(`${API_URL}/paused/${jobId}`, {})
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(`Successfully paused job ${jobId}`);
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const resumeJob = useCallback(
        async (jobId: number) => {
            return APIService.delete(`${API_URL}/paused/${jobId}`)
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(`Successfully resumed job ${jobId}`);
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const relaunchJob = useCallback(
        async (jobId: number) => {
            return APIService.post(
                `${API_URL}/${jobId}`,
                {},
                {
                    headers: {
                        Accept: "*/*", // API returns plain text
                    },
                }
            )
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(`Successfully relaunched job ${jobId}`);
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const switchJoqQueue = useCallback(
        async (jobId: number, queueId: number) => {
            return APIService.post(`/client/q/${queueId}/${jobId}`, {})
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(
                        `Successfully switch job ${jobId} to queue ${queueId}`
                    );
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const launchJob = useCallback(
        async (jobLauchParameters: JobLaunchParameters) => {
            return APIService.post(API_URL, jobLauchParameters)
                .then(() => {
                    fetchJobInstances(
                        page,
                        rowsPerPage,
                        sortOrder!,
                        queryLiveInstances,
                        filterList
                    );
                    displaySuccess(`Successfully created job`);
                })
                .catch(displayError);
        },
        [
            page,
            rowsPerPage,
            sortOrder,
            filterList,
            queryLiveInstances,
            fetchJobInstances,
            displayError,
            displaySuccess,
        ]
    );

    const fetchLogsStdout = useCallback(
        async (jobId: number) => {
            return APIService.get(
                `${API_URL}/${jobId}/stdout`,
                {
                    headers: {
                        Accept: "*/*", // API returns plain text
                    },
                },
                false
            ).catch(displayError);
        },
        [displayError]
    );

    const fetchLogsStderr = useCallback(
        async (jobId: number) => {
            return APIService.get(
                `${API_URL}/${jobId}/stderr`,
                {
                    headers: {
                        Accept: "*/*", // API returns plain text
                    },
                },
                false
            ).catch(displayError);
        },
        [displayError]
    );

    const fetchFiles = useCallback(
        async (jobId: number) => {
            return APIService.get(`${API_URL}/${jobId}/files`).catch(
                displayError
            );
        },
        [displayError]
    );

    const fetchFileContent = useCallback(
        async (fileId: number) => {
            return APIService.get(
                `${API_URL}/files/${fileId}`,
                {
                    headers: {
                        Accept: "*/*", // API returns plain text
                    },
                },
                false
            ).catch(displayError);
        },
        [displayError]
    );

    return {
        count,
        page,
        rowsPerPage,
        sortOrder,
        filterList,
        queryLiveInstances,
        jobInstances,
        fetchJobInstances,
        launchJob,
        killJob,
        pauseJob,
        resumeJob,
        relaunchJob,
        queues,
        fetchQueues,
        switchJoqQueue,
        fetchLogsStdout,
        fetchLogsStderr,
        fetchFiles,
        fetchFileContent,
    };
};

export default useJobInstanceAPI;
