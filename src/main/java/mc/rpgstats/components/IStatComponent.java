package mc.rpgstats.components;

import nerdhub.cardinal.components.api.component.Component;

interface IStatComponent extends Component {
    int getValue();

    void setValue(int newValue);
}
