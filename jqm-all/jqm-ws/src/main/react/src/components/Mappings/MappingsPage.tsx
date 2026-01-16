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
import { useTranslation } from "react-i18next";
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
import { useMUIDataTableTextLabels } from "../../utils/useMUIDataTableTextLabels";

const MappingsPage: React.FC = () => {
    const { t } = useTranslation();
    const muiTextLabels = useMUIDataTableTextLabels(t("mappings.noMatch"));
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
    }, [canUserAccess]);

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
            label: t("mappings.node"),
            options: {
                hint: t("mappings.hints.node"),
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
            label: t("mappings.queue"),
            options: {
                hint: t("mappings.hints.queue"),
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
            label: t("mappings.pollingInterval"),
            options: {
                hint: t("mappings.hints.pollingInterval"),
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
            label: t("mappings.maxConcurrentInstances"),
            options: {
                hint: t("mappings.hints.maxConcurrentInstances"),
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
            label: t("mappings.enabled"),
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
            label: t("common.actions"),
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
                    canUserAccess(PermissionObjectType.qmapping, PermissionAction.delete),
                    [],
                    t
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: muiTextLabels,
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.qmapping, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.qmapping, PermissionAction.create) &&
                    <>
                        <Tooltip title={t("common.add")}>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                        </Tooltip>
                        {showDialog && (
                            <CreateMappingDialog
                                closeDialog={() => setShowDialog(false)}
                                createMapping={createMapping}
                                nodes={nodes!!}
                                queues={queues!!}
                            />
                        )}
                    </>}
                <Tooltip title={t("common.refresh")}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => refresh()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={t("common.help")}>
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
                title={t("mappings.documentation.title")}
                header={t("mappings.documentation.header")}
                descriptionParagraphs={[
                    t("mappings.documentation.description")
                ]}
            />
            <MUIDataTable
                title={t("mappings.title")}
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
