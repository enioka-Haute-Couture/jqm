import React, {
    useMemo,
    useEffect,
    useState,
    useCallback,
    useRef,
} from "react";
import {
    Container,
    Grid,
    IconButton,
    Switch,
    Tooltip,
} from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import DoneIcon from "@material-ui/icons/Done";
import BlockIcon from "@material-ui/icons/Block";
import DeleteIcon from "@material-ui/icons/Delete";
import CreateIcon from "@material-ui/icons/Create";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import TextField from "@material-ui/core/TextField/TextField";
import useQueueCrudApi from "./useQueueCrudApi";
import { CreateQueueModal } from "./CreateQueueModal";

const QueuesPage: React.FC = () => {
    const [showModal, setShowModal] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingDefaultQueue, setEditingDefaultQueue] = useState<any | false>(
        false
    );
    const editingDescriptionInputRef = useRef(null);
    const editingQueueNameInputRef = useRef(null);

    const {
        queues,
        fetchQueues,
        createQueue,
        updateQueue,
        deleteQueues,
    } = useQueueCrudApi();

    useEffect(() => {
        fetchQueues();
    }, []);

    const updateRow = useCallback(
        (queueId: number) => {
            const { value: queueName } = editingQueueNameInputRef.current!;
            const { value: description } = editingDescriptionInputRef.current!;
            if (queueId && queueName && description) {
                updateQueue(
                    queueId,
                    queueName,
                    description,
                    editingDefaultQueue
                ).then(() => setEditingRowId(null));
            }
        },
        [updateQueue, editingDefaultQueue]
    );

    /*
     * Render cell containing boolean value
     */
    const renderBooleanCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <Switch
                    checked={editingDefaultQueue}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) =>
                        setEditingDefaultQueue(event.target.checked)
                    }
                />
            );
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

    /**
     * Render cell with action buttons
     */
    const renderActionsCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            const queueId = tableMeta.rowData ? tableMeta.rowData[0] : null;
            return (
                <>
                    <Tooltip title={"Cancel changes"}>
                        <IconButton
                            color="default"
                            aria-label={"cancel"}
                            onClick={() => setEditingRowId(null)}
                        >
                            <CancelIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Save changes"}>
                        <IconButton
                            color="default"
                            aria-label={"save"}
                            onClick={() => updateRow(queueId)}
                        >
                            <SaveIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        } else {
            return (
                <>
                    <Tooltip title={"Edit line"}>
                        <IconButton
                            color="default"
                            aria-label={"edit"}
                            onClick={() => {
                                setEditingRowId(tableMeta.rowIndex);
                                setEditingDefaultQueue(tableMeta.rowData[3]);
                            }}
                        >
                            <CreateIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Delete line"}>
                        <IconButton
                            color="default"
                            aria-label={"delete"}
                            onClick={() => {
                                const [queueId] = tableMeta.rowData;
                                deleteQueues([queueId]);
                            }}
                        >
                            <DeleteIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        }
    };

    const renderStringCell = (inputRef: any) => (
        value: any,
        tableMeta: any
    ) => {
        const key = tableMeta.rowData[0];
        if (editingRowId === tableMeta.rowIndex) {
            const defaultDescription = tableMeta.rowData
                ? tableMeta.rowData[tableMeta.columnIndex]
                : "";
            return (
                <TextField
                    key={key}
                    id="standard-basic"
                    defaultValue={defaultDescription}
                    inputRef={inputRef}
                    fullWidth
                    margin="normal"
                    inputProps={{
                        style: { fontSize: "0.875rem" },
                    }}
                />
            );
        } else {
            return (
                <TextField
                    key={key}
                    defaultValue={value}
                    fullWidth
                    margin="normal"
                    InputProps={{ disableUnderline: true }}
                    inputProps={{
                        // the actual input element
                        readOnly: true,
                        style: {
                            cursor: "default",
                            fontSize: "0.875rem",
                        },
                    }}
                />
            );
        }
    };

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "name",
            label: "Name",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell(editingQueueNameInputRef),
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell(editingDescriptionInputRef),
            },
        },
        {
            name: "defaultQueue",
            label: "Is default",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell,
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell,
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowModal(true)}
                            >
                                <AddCircleIcon />
                            </IconButton>
                            <CreateQueueModal
                                showModal={showModal}
                                closeModal={() => setShowModal(false)}
                                createQueue={createQueue}
                            />
                        </>
                    </Tooltip>
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchQueues()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const queueIds = data.map(({ index }) => {
                const queue = queues ? queues[index] : null;
                return queue ? queue.id : null;
            });
            deleteQueues(queueIds);
        },
        //filterType: 'checkbox',
    };

    return queues ? (
        <Container>
            <MUIDataTable
                title={"Queues"}
                data={queues}
                columns={columns}
                options={options}
            />
        </Container>
    ) : (
        <Grid container justify="center">
            <CircularProgress />
        </Grid>
    );
};

export default QueuesPage;
