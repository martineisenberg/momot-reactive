package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.UnitApplicationVariable;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

public class Executor {
   private final Module module;
   private ModelRuntimeEnvironment mre = null;
   private final Engine engine;

   private Executor(final Module module) {
      this.module = module;
      this.engine = new EngineImpl();
   }

   public Executor(final String moduleUri) {
      final HenshinResourceSet hrs = new HenshinResourceSet();
      this.module = hrs.getModule(moduleUri);
      this.engine = new EngineImpl();
   }

   public Executor copy() {
      return new Executor(this.module);
   }

   public boolean execute(final ITransformationVariable var) {
      final Unit unit = this.module.getUnit(var.getUnit().getName());

      final UnitApplication application = new UnitApplicationImpl(engine, mre.getGraph(), unit, null);

      for(final Parameter param : var.getUnit().getParameters()) {
         application.setParameterValue(param.getName(), var.getParameterValue(param));
      }

      return application.execute(null);
   }

   public ITransformationVariable execute(final String unitName, final EGraph graph, final Map<String, Object> params) {
      final Unit unit = this.module.getUnit(unitName);
      final UnitApplicationVariable applicationVar = new UnitApplicationVariable(engine, graph, unit, null);

      for(final Entry<String, Object> e : params.entrySet()) {
         applicationVar.setParameterValue(e.getKey(), e.getValue());
      }

      // (final Engine engine, final EGraph graph, final Rule rule,
      // final Assignment partialMatch)
      applicationVar.execute(null);

      return applicationVar;
   }

   public void setModelRuntimeEnvironment(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
   }

}
