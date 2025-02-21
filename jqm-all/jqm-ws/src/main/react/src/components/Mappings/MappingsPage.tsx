import React, { useCallback, useEffect, useRef, useState } from "react";
import {
    Container,
    Grid,
    IconButton,
    MenuItem,
    Tooltip,
} from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { CreateMappingDialog } from "./CreateMappingDialog";
import useMappingAPI from "./MappingAPI";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { renderArrayCell } from "../TableCells/renderArrayCell";
import useNodesApi from "../Nodes/NodesApi";
import useQueueAPI from "../Queues/QueueAPI";
import { Node } from "../Nodes/Node";
import { Queue } from "../Queues/Queue";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { HelpDialog } from "../HelpDialog";
import { setPageTitle } from "../../utils/title";

const MappingsPage: React.FC = () => {
    const [showDialog, setShowDialog] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);

    const [nodeId, setNodeId] = useState<number | null>(null);
    const [queueId, setQueueId] = useState<number | null>(null);
    const [enabled, setEnabled] = useState<boolean>(false);
    const pollingIntervalInputRef = useRef(null);
    const nbThreadInputRef = useRef(null);

    const {
        mappings,
        fetchMappings,
        createMapping,
        updateMapping,
        deleteMappings,
    } = useMappingAPI();

    const { nodes, fetchNodes } = useNodesApi();

    const { canUserAccess } = useAuth();

    const { queues, fetchQueues } = useQueueAPI();

    const refresh = () => {
        fetchNodes();
        fetchQueues();
        fetchMappings();
    };

    useEffect(() => {
        if ((canUserAccess(PermissionObjectType.qmapping, PermissionAction.read) &&
            canUserAccess(PermissionObjectType.queue, PermissionAction.read) &&
            canUserAccess(PermissionObjectType.node, PermissionAction.read))) {
            refresh();
        }
        setPageTitle('Mappings');
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [mappingId] = tableMeta.rowData;
            deleteMappings([mappingId]);
        },
        [deleteMappings]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [mappingId] = tableMeta.rowData;
            const { value: pollingInterval } = pollingIntervalInputRef.current!;
            const { value: nbThread } = nbThreadInputRef.current!;
            const nodeName = nodes?.find((x) => x.id === nodeId)?.name;
            const queueName = queues?.find((x) => x.id === queueId)?.name;
            if (
                mappingId &&
                pollingInterval &&
                nbThread &&
                nodeId &&
                queueId &&
                queueName &&
                nodeName
            ) {
                updateMapping({
                    id: mappingId,
                    enabled: enabled,
                    nodeId: nodeId,
                    queueId: queueId,
                    nodeName: nodeName,
                    queueName: queueName,
                    nbThread: +nbThread,
                    pollingInterval: +pollingInterval,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateMapping, enabled, queueId, nodeId, nodes, queues]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);

    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setNodeId(tableMeta.rowData[1]);
        setQueueId(tableMeta.rowData[2]);
        setEnabled(tableMeta.rowData[5]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

    const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "nodeId",
            label: "Node*",
            options: {
                hint: "The node which will poll the queue",
                filter: false,
                sort: false,
                customBodyRender: renderArrayCell(
                    editingRowId,
                    nodes
                        ? nodes!.map((node: Node) => (
                            <MenuItem key={node.id} value={node.id}>
                                {node.name}
                            </MenuItem>
                        ))
                        : [],
                    (element: number) =>
                        nodes?.find((x) => x.id === element)?.name || "",
                    nodeId,
                    setNodeId,
                    false
                ),
            },
        },
        {
            name: "queueId",
            label: "Queue*",
            options: {
                hint: "The queue to poll",
                filter: false,
                sort: false,
                customBodyRender: renderArrayCell(
                    editingRowId,
                    queues
                        ? queues!.map((queue: Queue) => (
                            <MenuItem key={queue.id} value={queue.id}>
                                {queue.name}
                            </MenuItem>
                        ))
                        : [],
                    (element: number) =>
                        queues?.find((x) => x.id === element)?.name || "",
                    queueId,
                    setQueueId,
                    false
                ),
            },
        },
        {
            name: "pollingInterval",
            label: "Polling Interval (ms)*",
            options: {
                hint: "The polling interval, in milliseconds. Never go below one second. If updated on an active engine, it is applied to the next loop (not the current one).",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    pollingIntervalInputRef,
                    editingRowId,
                    false,
                    "number"
                ),
            },
        },
        {
            name: "nbThread",
            label: "Max concurrent running instances*",
            options: {
                hint: "The maximum number of parallel executions the node will allow for this queue (this translates directly as a max number of threads inside the engine).",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    nbThreadInputRef,
                    editingRowId,
                    false,
                    "number"
                ),
            },
        },
        {
            name: "enabled",
            label: "Enabled",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    enabled,
                    setEnabled
                ),
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: false,
                sort: false,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.qmapping, PermissionAction.update),
                    canUserAccess(PermissionObjectType.qmapping, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No mappings found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.qmapping, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.qmapping, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                            {showDialog && (
                                <CreateMappingDialog
                                    closeDialog={() => setShowDialog(false)}
                                    createMapping={createMapping}
                                    nodes={nodes!!}
                                    queues={queues!!}
                                />
                            )}
                        </>
                    </Tooltip>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => refresh()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Help"}>
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const mappingIds: number[] = [];
            data.forEach(({ index }) => {
                const mapping = mappings ? mappings[index] : null;
                if (mapping) {
                    mappingIds.push(mapping.id!);
                }
            });
            deleteMappings(mappingIds);
        },
    };

    if (!(canUserAccess(PermissionObjectType.qmapping, PermissionAction.read) &&
        canUserAccess(PermissionObjectType.queue, PermissionAction.read) &&
        canUserAccess(PermissionObjectType.node, PermissionAction.read))) {
        return <AccessForbiddenPage />
    }


    return mappings && nodes && queues ? (
        <Container maxWidth={false}>
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title="Mappings documentation"
                header="Mappings specify which nodes will poll which queues. They are basically nodes subscribing to queues."
                descriptionParagraphs={[
                    "On this page, one may associate nodes with queues. Running nodes will (by default) check every minute if there are changes in their mappings."
                ]}
            />
            <MUIDataTable
                title={"Mappings"}
                data={mappings}
                columns={columns}
                options={options}
            />
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

export default MappingsPage;
