import { useState, useCallback } from "react";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { Queue } from "./Queue";

export const useQueueAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const [queues, setQueues] = useState<Queue[] | null>();

    const fetchQueues = useCallback(async () => {
        return APIService.get("/q")
            .then((queues) => setQueues(queues))
            .catch(displayError);
    }, [displayError]);

    const createQueue = useCallback(
        async (newQueue: Queue) => {
            return APIService.post("/q", newQueue)
                .then(() => {
                    fetchQueues();
                    displaySuccess(`Successfully created queue: ${newQueue.name}`);
                })
                .catch(displayError);
        },
        [fetchQueues, displayError, displaySuccess]
    );

    const deleteQueues = useCallback(
        async (queueIds: number[]) => {
            return await Promise.all(
                queueIds.map((id) => APIService.delete("/q/" + id))
            )
                .then(() => {
                    fetchQueues();
                    displaySuccess(
                        `Successfully deleted queue${queueIds.length > 1 ? "s" : ""}`);
                })
                .catch(displayError);
        },
        [fetchQueues, displayError, displaySuccess]
    );

    const updateQueue = useCallback(
        async (
            queue: Queue
        ) => {
            return APIService.put("/q/" + queue.id, queue)
                .then(() => {
                    fetchQueues();
                    displaySuccess("Successfully saved queue");
                })
                .catch(displayError);
        },
        [fetchQueues, displayError, displaySuccess]
    );
    return {
        queues,
        fetchQueues,
        createQueue,
        updateQueue,
        deleteQueues,
    };
};

export default useQueueAPI;
