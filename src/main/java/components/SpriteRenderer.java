package components;

import yashima.Component;

public class SpriteRenderer extends Component {
    private boolean firstTime = false;

    @Override
    public void start() {
        System.out.println("Starting the system");
    }

    @Override
    public void update(float dt) {
        if(!firstTime) {
            System.out.println("Update in progress");

            firstTime = true;
        }
    }
}