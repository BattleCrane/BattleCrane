package polytech;

import game.battleFields.BattleManager;
import botInterface.Bot;
import botInterface.priority.PriorityUnit;
import polytech.polyCombinations.PolyCombinator;
import polytech.steps.Step;
import controllers.ControllerMatchMaking;
import polytech.steps.UnityStep;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PolytechnicBot implements Bot {
    private final Logger logger = Logger.getLogger(PolytechnicBot.class.getName());
    {{
       logger.addHandler(new ConsoleHandler(){{
           this.setLevel(Level.INFO);
       }});
    }}

    private final ControllerMatchMaking controllerMatchMaking;
    private final PolyCombinator combinator;

    public static int step = 0;

    //Конструктор:
    public PolytechnicBot(ControllerMatchMaking controllerMatchMaking) {
        this.controllerMatchMaking = controllerMatchMaking;
        combinator = new PolyCombinator(controllerMatchMaking);
    }

    @Override
    public int getCountOfStep() {
        return 0;
    }

    @Override
    public void setCountOfStep(int countOfStep) {

    }

    @Override
    public List<Step> loadSteps(BattleManager battleManager) {
        List<Step> steps = new ArrayList<>();
        for (PriorityUnit p: combinator.chooseDevelopment().getUnits()){
            steps.add(new UnityStep(battleManager, p));
        }
        steps.addAll(combinator.chooseAttacks());
        return steps;
    }
}
