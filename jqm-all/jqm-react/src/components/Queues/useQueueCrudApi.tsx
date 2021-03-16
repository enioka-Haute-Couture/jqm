import { useState, useCallback } from "react";
import { useSnackbar } from "notistack";
import APIService from "../../utils/APIService";
import { Queue } from "./Queue";

const useQueueCrudApi = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [queues, setQueues] = useState<any[] | null>();

    const fetchQueues = useCallback(async () => {
        return APIService.get("/q")
            .then((response) => {
                console.log(response);
                setQueues(response);
            })
            .catch((reason) => {
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    }, [enqueueSnackbar]);

    const createQueue = useCallback(
        async (newQueue: Queue) => {
            return APIService.post("/q", newQueue)
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar(
                        `Successfully created queue: ${newQueue.name}`,
                        {
                            variant: "success",
                        }
                    );
                })
                .catch((reason) => {
                    enqueueSnackbar(
                        "An error occured, please contact support support@enioka.com for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [enqueueSnackbar, fetchQueues]
    );

    const deleteQueues = useCallback(
        async (queueIds: any[]) => {
            return await Promise.all(
                queueIds.map((id) => APIService.delete("/q/" + id))
            )
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar(
                        `Successfully deleted queue${
                            queueIds.length > 1 ? "s" : ""
                        }`,
                        {
                            variant: "success",
                        }
                    );
                })
                .catch((reason) => {
                    console.debug(reason);
                    enqueueSnackbar(
                        "An error occured, please contact support support@enioka.com for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [enqueueSnackbar, fetchQueues]
    );

    const updateQueue = useCallback(
        async (
            queueId: Number,
            queueName: string,
            queueDescription: string,
            defaultQueue: Boolean
        ) => {
            const request: Queue = {
                id: queueId,
                name: queueName,
                description: queueDescription,
                defaultQueue: defaultQueue,
            };
            return APIService.put("/q/" + request["id"], request)
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar("Successfully saved queue", {
                        variant: "success",
                    });
                })
                .catch((reason) => {
                    enqueueSnackbar(
                        "An error occured, please contact support for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [fetchQueues, enqueueSnackbar]
    );
    return { queues, fetchQueues, createQueue, updateQueue, deleteQueues };
};

export default useQueueCrudApi;
