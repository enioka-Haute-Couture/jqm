import React from "react";
import MenuItem from "@material-ui/core/MenuItem";
import ListSubheader from "@material-ui/core/ListSubheader";
import FormControl from "@material-ui/core/FormControl";
import Select from "@material-ui/core/Select";
import { resourceTemplates } from "./resourceTemplates";
import { JndiResource } from "./JndiResource";

const getResourceTemplate = (key: string) => ({
    ...resourceTemplates[key],
    resourceKey: key,
});

const resourceTemplatesList = [
    {
        title: "Database Pools",
        resources: [
            getResourceTemplate("jndiOracle"),
            getResourceTemplate("jndiMySql"),
            getResourceTemplate("jndiPs"),
            getResourceTemplate("jndiHsqlDb"),
            getResourceTemplate("jndiOtherDb"),
        ],
    },
    {
        title: "Messages Q & QCF",
        resources: [
            getResourceTemplate("jndiMqQcf"),
            getResourceTemplate("jndiMqQ"),
            getResourceTemplate("jndiAmqQcf"),
            getResourceTemplate("jndiAmqQ"),
        ],
    },
    {
        title: "Locators",
        resources: [
            getResourceTemplate("jndiFile"),
            getResourceTemplate("jndiUrl"),
            getResourceTemplate("jndiString"),
        ],
    },
    {
        title: "SMTP Mail",
        resources: [getResourceTemplate("jndiMail")],
    },
    {
        title: "Generic Resource",
        resources: [getResourceTemplate("jndiGeneric")],
    },
];

export const ResourceDropDownMenu: React.FC<{
    menuPositiontRef: React.MutableRefObject<null>;
    show: boolean;
    handleSet: (val: boolean) => void;
    onOpen: () => void;
    onClose: () => void;
    onSelectResource: (resource: JndiResource) => void;
}> = ({ menuPositiontRef, show, onClose, onOpen, onSelectResource }) => {
    const getSelectGroupList = () => {
        const res: any[] = [];
        resourceTemplatesList.forEach(({ title, resources }) => {
            res.push(
                <ListSubheader key={title} disableSticky>
                    {title}
                </ListSubheader>
            );
            resources.map(({ name, resourceKey }) =>
                res.push(
                    <MenuItem key={resourceKey} value={resourceKey}>
                        New {name}
                    </MenuItem>
                )
            );
        });
        return res;
    };

    return (
        <FormControl ref={menuPositiontRef}>
            <Select
                id="select"
                style={{ display: "none" }}
                open={show}
                value=""
                defaultValue=""
                onOpen={() => onOpen()}
                onClose={() => onClose()}
                onChange={(event: React.ChangeEvent<{ value: unknown }>) => {
                    const val = event.target.value as string;
                    onSelectResource(resourceTemplates[val]);
                }}
                MenuProps={{
                    anchorEl: menuPositiontRef.current,
                    style: { marginTop: "5rem" },
                }}
            >
                {getSelectGroupList()}
            </Select>
        </FormControl>
    );
};
