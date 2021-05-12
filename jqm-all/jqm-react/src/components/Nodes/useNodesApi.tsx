import { useState, useCallback } from "react";
import { useSnackbar } from "notistack";
import APIService from "../../utils/APIService";
import { Node } from "./Node";

const useNodesApi = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [nodes, setNodes] = useState<Node[] | null>();
    const [nodeLogs, setNodeLogs] =
        useState<{
            nodeName: string;
            data: string;
        }>();

    const fetchNodes = useCallback(async () => {
        return APIService.get("/node")
            .then((nodes) => setNodes(nodes))
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
    }, [enqueueSnackbar]);

    const createNode = useCallback(
        async (node: Node) => {
            return APIService.post("/node", node)
                .then(() => {
                    fetchNodes();
                    enqueueSnackbar(
                        `Successfully created queue: ${node.name}`,
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
        [enqueueSnackbar, fetchNodes]
    );

    const deleteNodes = useCallback(
        async (nodes: Node[]) => updateNodes(nodes),
        [enqueueSnackbar, fetchNodes]
    );

    const updateNode = useCallback((node: Node) => updateNodes([node]), []);

    const updateNodes = useCallback(
        async (nodes: Node[]) => {
            return APIService.put("/node", nodes)
                .then(() => {
                    fetchNodes();
                    enqueueSnackbar("Successfully updated node", {
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
        [fetchNodes, enqueueSnackbar]
    );

    const fetchNodeLogs = useCallback(
        async (nodeName: string, latest: number = 200) => {
            return APIService.get(
                `/node/${nodeName}/log?latest=${latest}`,
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
        [enqueueSnackbar, updateNodes]
    );

    return {
        nodes,
        nodeLogs,
        fetchNodes,
        createNode,
        updateNode,
        deleteNodes,
        fetchNodeLogs,
    };
};

export default useNodesApi;
