package de.hbt.pwr.view.model.entries.sort;

import de.hbt.pwr.view.model.ViewProfile;
import de.hbt.pwr.view.util.PwrListUtil;

import java.util.List;

/**
 * Used on an enum; defines that there are profile entries that are
 * extractable for moving (with {@link PwrListUtil#move(List, int, int)}
 * @author nt nt@hbt.de
 * @since 29.08.2017.
 */
public interface MovableEntryExtractable {
    List<?> extractMovableEntry(ViewProfile viewProfile);
}
