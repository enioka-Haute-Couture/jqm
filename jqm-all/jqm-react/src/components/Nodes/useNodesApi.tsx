import { useState, useCallback } from "react";
import { useSnackbar } from "notistack";
import APIService from "../../utils/APIService";
import { Node } from "./Node";

const useNodesApi = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [nodes, setNodes] = useState<Node[] | null>();
    const [nodeLogs, setNodeLogs] = useState<string[] | null>();

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
        async (nodeIds: any[]) => {
            return await Promise.all(
                nodeIds.map((id) => APIService.delete("/node/" + id))
            )
                .then(() => {
                    fetchNodes();
                    enqueueSnackbar(
                        `Successfully deleted node${
                            nodeIds.length > 1 ? "s" : ""
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
        async (nodeId: number, latest: number) => {
            return APIService.get(`/node/${nodeId}/log?latest=${latest}`)
                .then((logs) => setNodeLogs(logs))
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
        [enqueueSnackbar]
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
