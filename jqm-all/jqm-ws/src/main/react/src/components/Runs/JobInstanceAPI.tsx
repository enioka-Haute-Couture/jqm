import { MUISortOptions } from "mui-datatables";
import { useCallback, useState } from "react";
import { JobInstance } from "./JobInstance";
import { JobLaunchParameters } from "./JobLaunchParameters";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import useQueueAPI from "../Queues/QueueAPI";

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
    applicationName?: string;
    statuses: string[];
}

export const useJobInstanceAPI = (emptyFilterList: string[][]) => {
    const { displayError, displaySuccess } = useNotificationService();

    const [jobInstances, setJobInstances] = useState<JobInstance[] | null>();
    const [count, setCount] = useState<number>(0);
    const [rowsPerPage, setRowsPerPage] = useState<number>(10);
    const [page, setPage] = useState<number>(0);
    const [sortOrder, setSortOrder] = useState<MUISortOptions>({
        name: "id",
        direction: "desc",
    });
    const [filterList, setFilterList] = useState<string[][]>(emptyFilterList);
    const [queryLiveInstances, setQueryLiveInstances] =
        useState<boolean>(false);

    const { queues, fetchQueues } = useQueueAPI();

    const fetchJobInstances = useCallback(
        async (
            page: number,
            rowsPerPage: number,
            sortOrder: MUISortOptions,
            queryLiveInstances: boolean,
            filterList: string[][]
        ) => {
            setPage(page);
            setRowsPerPage(rowsPerPage);
            setSortOrder(sortOrder);
            setQueryLiveInstances(queryLiveInstances);

            let filterQuery: JobInstanceFilters = { statuses: [] };

            setFilterList(filterList);

            if (filterList[0]?.length > 0) {
                filterQuery.jobInstanceId = filterList[0][0];
            }
            if (filterList[1]?.length > 0) {
                filterQuery.applicationName = filterList[1][0];
            }
            if (filterList[2]?.length > 0) {
                filterQuery.queueId = queues!.find(
                    (queue) => queue.name === filterList[2][0]
                )?.id;
            }
            if (filterList[3]?.length > 0) {
                filterQuery.statuses = [filterList[3][0]];
            }
            if (filterList[7]?.length > 0) {
                filterQuery.user = filterList[7][0];
            }
            if (filterList[8]?.length > 0) {
                filterQuery.parentId = filterList[8][0];
            }
            if (filterList[10]?.length > 0) {
                filterQuery.sessionId = filterList[10][0];
            }

            var sortColumnName = sortOrder.name.toUpperCase();
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
                        sortOrder.direction === "asc"
                            ? "ASCENDING"
                            : "DESCENDING",
                },
            ];

            let request = {
                ...filterQuery,
                firstRow: rowsPerPage * page,
                pageSize: rowsPerPage,
                queryLiveInstances: queryLiveInstances,
                queryHistoryInstances: !queryLiveInstances,
                sortby: sortBy,
            };

            return APIService.post(`${API_URL}/query/`, request)
                .then((response) => {
                    setJobInstances(response["instances"]);
                    setCount(response["resultSize"]);
                })
                .catch(displayError);
        },
        [displayError, queues]
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
            return APIService.get(`${API_URL}/${jobId}/stdout`, {
                headers: {
                    Accept: "*/*", // API returns plain text
                },
            }).catch(displayError);
        },
        [displayError]
    );

    const fetchLogsStderr = useCallback(
        async (jobId: number) => {
            return APIService.get(`${API_URL}/${jobId}/stderr`, {
                headers: {
                    Accept: "*/*", // API returns plain text
                },
            }).catch(displayError);
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
    };
};

export default useJobInstanceAPI;
