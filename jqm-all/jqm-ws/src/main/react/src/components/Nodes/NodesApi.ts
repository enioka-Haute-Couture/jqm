import { useCallback, useState } from "react";
import { Node } from "./Node";
import APIService from "../../utils/APIService";
import { useNotificationService } from "../../utils/NotificationService";

const API_URL = "/admin/node"

const useNodesApi = () => {
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

    const updateNodes = useCallback(
        async (nodes: Node[]) => {
            return APIService.put(API_URL, nodes)
                .then(() => {
                    fetchNodes();
                    displaySuccess("Successfully updated node");
                })
                .catch(displayError);
        },
        [fetchNodes, displaySuccess, displayError]
    );

    const updateNode = useCallback(
        (node: Node) => updateNodes([node]),
        [updateNodes]
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
                .then((response) => response.text())
                .then((textData) =>
                    setNodeLogs({ nodeName: nodeName, data: textData })
                )
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
