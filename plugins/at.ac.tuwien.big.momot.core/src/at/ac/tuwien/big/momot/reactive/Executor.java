package at.ac.tuwien.big.momot.reactive;

import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;

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

   public Executor(final String moduleUri) {
      final HenshinResourceSet hrs = new HenshinResourceSet();
      this.module = hrs.getModule(moduleUri);
      this.engine = new EngineImpl();
   }

   public boolean execute(final ITransformationVariable var) {
      final Unit unit = this.module.getUnit(var.getUnit().getName());

      final UnitApplication application = new UnitApplicationImpl(engine, mre.getGraph(), unit, null);

      for(final Parameter param : var.getUnit().getParameters()) {
         application.setParameterValue(param.getName(), var.getParameterValue(param));
      }

      return application.execute(null);
   }

   public void setModelRuntimeEnvironment(final ModelRuntimeEnvironment mre) {
      this.mre = mre;
   }
}
