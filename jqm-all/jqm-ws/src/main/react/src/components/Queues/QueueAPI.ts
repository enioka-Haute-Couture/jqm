import { useCallback, useState } from "react";
import { Queue } from "./Queue";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/q";

export const useQueueAPI = () => {
    const { displayError, displaySuccess } = useNotificationService();

    const [queues, setQueues] = useState<Queue[] | null>();

    const fetchQueues = useCallback(async () => {
        return APIService.get(API_URL)
            .then((queues) => setQueues(queues))
            .catch(displayError);
    }, [displayError]);

    const createQueue = useCallback(
        async (newQueue: Queue) => {
            return APIService.post(API_URL, newQueue)
                .then(() => {
                    fetchQueues();
                    displaySuccess(
                        `Successfully created queue: ${newQueue.name}`
                    );
                })
                .catch(displayError);
        },
        [fetchQueues, displayError, displaySuccess]
    );

    const deleteQueues = useCallback(
        async (queueIds: number[]) => {
            return await Promise.all(
                queueIds.map((id) => APIService.delete(`${API_URL}/${id}`))
            )
                .then(() => {
                    fetchQueues();
                    displaySuccess(
                        `Successfully deleted queue${queueIds.length > 1 ? "s" : ""
                        }`
                    );
                })
                .catch(displayError);
        },
        [fetchQueues, displayError, displaySuccess]
    );

    const updateQueue = useCallback(
        async (queue: Queue) => {
            return APIService.put(`${API_URL}/${queue.id}`, queue)
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
