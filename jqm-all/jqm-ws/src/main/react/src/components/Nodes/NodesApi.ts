import { useCallback, useState } from "react";
import { Node } from "./Node";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";
import { useTranslation } from "react-i18next";

const API_URL = "/admin/node";

const useNodesApi = () => {
    const { t } = useTranslation();
    const { displayError, displaySuccess } = useNotificationService();

    const [nodes, setNodes] = useState<Node[] | null>();
    const [nodeLogs, setNodeLogs] = useState<{
        nodeName: string;
        data: string;
    }>();

    const fetchNodes = useCallback(async () => {
        return APIService.get(API_URL)
            .then((nodes) => setNodes(nodes))
            .catch(displayError);
    }, [displayError]);

    const updateNode = useCallback(
        (node: Node) => {
            return APIService.put(API_URL, node)
                .then(() => {
                    fetchNodes();
                    displaySuccess(
                        t("nodes.messages.successUpdate", { name: node.name })
                    );
                })
                .catch(displayError);
        },
        [fetchNodes, displaySuccess, displayError, t]
    );

    const fetchNodeLogs = useCallback(
        async (nodeName: string, latest: number = 200) => {
            return APIService.get(
                `${API_URL}/${nodeName}/log?latest=${latest}`,
                {
                    headers: {
                        Accept: "application/octet-stream",
                    },
                },
                false
            )
                .then((response) => {
                    setNodeLogs({ nodeName: nodeName, data: response });
                })
                .catch(displayError);
        },
        [displayError]
    );

    return {
        nodes,
        nodeLogs,
        setNodeLogs,
        fetchNodes,
        updateNode,
        fetchNodeLogs,
    };
};

export default useNodesApi;
