package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.moea.experiment.executor.listener.AbstractProgressListener;
import at.ac.tuwien.big.momot.reactive.planningstrategy.SearchConfiguration;
import at.ac.tuwien.big.momot.reactive.result.SearchResult;

import java.util.List;

public interface IReactiveSearchInstance {

   public SearchResult performSearch(SearchConfiguration conf);

   SearchResult performSearch(SearchConfiguration conf, List<AbstractProgressListener> listeners);
}
