import React, { useState, useCallback, useRef, useMemo } from "react";
import {
    Container,
    Grid,
    IconButton,
    Tooltip,
    Badge,
    Typography,
} from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SettingsIcon from "@material-ui/icons/Settings";
import {
    renderInputCell,
    renderBooleanCell,
    renderActionsCell,
} from "../TableCells";
import { ResourceDialog } from "./ResourceDialog";
import useJndiApi from "./useJndiApi";
import { JndiResource } from "./JndiResource";
import { ResourceDropDownMenu } from "./ResourceDropDownMenu";

export const JndiPage: React.FC = () => {
    const [showDropDown, setShowDropDown] = useState(false);
    const dropDownMenuPositionRef = useRef(null);
    const [currentSelectedResource, setCurrentSelectedResource] =
        useState<JndiResource | null>(null);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const selectedNameRef = useRef(null);
    const selectedTypeRef = useRef(null);
    const selectedFactoryRef = useRef(null);
    const selectedDescriptionRef = useRef(null);
    const [isSingleton, setIsSingleton] = useState<boolean>(false);
    const [inMemoryResources, setInMemoryResources] = useState<JndiResource[]>(
        []
    );

    const {
        resources,
        fetchResources,
        createResource,
        updateResource,
        deleteResource,
    } = useJndiApi();

    // useEffect(() => {
    //     fetchResources();
    // }, [fetchResources]);

    const resourcesesMemo = useMemo(() => {
        return [...resources, ...inMemoryResources];
    }, [resources, inMemoryResources]);

    const handleOnDelete = useCallback(
        (tableMeta) => {
            const [paramId] = tableMeta.rowData;
            deleteResource([paramId]);
        },
        [deleteResource]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
            const [resourceId, auth, parameters] = tableMeta.rowData;
            const { value: name } = selectedNameRef.current!;
            const { value: type } = selectedTypeRef.current!;
            const { value: factory } = selectedFactoryRef.current!;
            const { value: description } = selectedDescriptionRef.current!;

            if (resourceId && name && type && factory) {
                updateResource({
                    id: resourceId,
                    name: name,
                    type: type,
                    auth: auth,
                    description: description,
                    factory: factory,
                    singleton: isSingleton,
                    parameters: parameters,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateResource, isSingleton]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback((tableMeta) => {
        setIsSingleton(tableMeta.rowData[7]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "auth",
            options: {
                display: "excluded",
            },
        },
        {
            name: "parameters",
            options: {
                display: "excluded",
            },
        },
        {
            name: "name",
            label: "JNDI Alias",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedNameRef,
                    editingRowId
                ),
            },
        },
        {
            name: "type",
            label: "Type",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedTypeRef,
                    editingRowId
                ),
            },
        },
        {
            name: "factory",
            label: "Factory",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedFactoryRef,
                    editingRowId
                ),
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    selectedDescriptionRef,
                    editingRowId
                ),
            },
        },
        {
            name: "singleton",
            label: "Singleton",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    isSingleton,
                    setIsSingleton
                ),
            },
        },
        {
            name: "parameters",
            label: "Parameters",
            options: {
                filter: true,
                sort: false,
                customBodyRender: (value: any, tableMeta: any) => {
                    const parameters = tableMeta.rowData[2];
                    const badge = (
                        <Badge
                            badgeContent={parameters ? parameters.length : 0}
                            color="primary"
                        >
                            <SettingsIcon />
                        </Badge>
                    );
                    return editingRowId === tableMeta.rowIndex ? (
                        <Tooltip
                            title={
                                editingRowId === tableMeta.rowIndex
                                    ? "Click to edit parameters"
                                    : ""
                            }
                        >
                            <span
                                style={{ cursor: "pointer" }}
                                onClick={() => {
                                    const [roleId] = tableMeta.rowData;
                                    setCurrentSelectedResource(
                                        tableMeta.rowData
                                    );
                                }}
                            >
                                {badge}
                            </span>
                        </Tooltip>
                    ) : (
                        badge
                    );
                },
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit
                    // [
                    //     {
                    //         title: "Edit Parameters",
                    //         addIcon: (tableMeta: any) => {
                    //             const parameters = tableMeta.rowData[2];
                    //             setCurrentSelectedResource(tableMeta.rowData);
                    //             return (
                    //                 <Badge
                    //                     badgeContent={
                    //                         parameters ? parameters.length : 0
                    //                     }
                    //                     color="primary"
                    //                 >
                    //                     <SettingsIcon />
                    //                 </Badge>
                    //             );
                    //         },
                    //         action: (tableMeta: any) =>
                    //             setCurrentSelectedResource(tableMeta.rowData),
                    //     },
                    // ]
                ),
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
                    <>
                        <Tooltip title={"Create new JNDI resource"}>
                            <IconButton
                                color="default"
                                aria-label={"Create new JNDI resource"}
                                onClick={() => setShowDropDown(true)}
                            >
                                <AddCircleIcon />
                            </IconButton>
                        </Tooltip>
                        <ResourceDropDownMenu
                            menuPositiontRef={dropDownMenuPositionRef}
                            show={showDropDown}
                            handleSet={setShowDropDown}
                            onOpen={() => setShowDropDown(true)}
                            onClose={() => setShowDropDown(false)}
                            onSelectResource={(newResource: JndiResource) => {
                                const newArr = [...inMemoryResources];
                                newArr.push(newResource);
                                setInMemoryResources(newArr);
                                setShowDropDown(false);
                            }}
                        />
                    </>

                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchResources()}
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
            const paramIds: number[] = [];
            data.forEach(({ index }) => {
                const resource = resources ? resources[index] : null;
                if (resource) {
                    paramIds.push(resource.id!);
                }
            });
            deleteResource(paramIds);
        },
    };

    return resources ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={"JNDI Resources"}
                data={resourcesesMemo}
                columns={columns}
                options={options}
            />
            <ResourceDialog
                selectedResource={currentSelectedResource}
                onChangeResource={(resource: JndiResource) => {
                    const val = inMemoryResources.find((e) => resource.name);
                    const newArr = [...inMemoryResources];
                }}
                closeDialog={() => setCurrentSelectedResource(null)}
            />
        </Container>
    ) : (
        <Grid container justify="center">
            <CircularProgress />
        </Grid>
    );
};
