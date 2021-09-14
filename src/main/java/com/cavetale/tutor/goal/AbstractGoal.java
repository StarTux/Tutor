package com.cavetale.tutor.goal;

import com.cavetale.tutor.session.PlayerQuest;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
/**
 * This class implements the progress related function and only
 * requires a call to the constructor to set them up.
 * @param P the GoalProgress subclass
 */
public abstract class AbstractGoal<P extends GoalProgress> implements Goal {
    @NonNull protected final Class<P> progressType;
    @NonNull protected final Supplier<P> progressConstructor;

    @Override
    public final P newProgress() {
        return progressConstructor.get();
    }

    @Override
    public final P getProgress(PlayerQuest playerQuest) {
        return playerQuest.getProgress(progressType, progressConstructor::get);
    }
}
