package info.archinnov.achilles.iterator;

import static info.archinnov.achilles.dao.AbstractDao.DEFAULT_LENGTH;
import info.archinnov.achilles.consistency.AchillesConfigurableConsistencyLevelPolicy;
import info.archinnov.achilles.entity.execution_context.SafeExecutionContext;
import info.archinnov.achilles.entity.type.ConsistencyLevel;
import info.archinnov.achilles.exception.AchillesException;
import me.prettyprint.hector.api.beans.Composite;

/**
 * AbstractAchillesSliceIterator
 * 
 * @author DuyHai DOAN
 * 
 */
public abstract class AbstractAchillesSliceIterator
{
	protected boolean reversed;
	protected int count = DEFAULT_LENGTH;
	protected int columns = 0;
	protected AchillesConfigurableConsistencyLevelPolicy policy;
	protected String columnFamily;
	protected ConsistencyLevel readConsistencyLevelAtInitialization;

	protected Composite start;
	protected ColumnSliceFinish finish;

	protected AbstractAchillesSliceIterator(AchillesConfigurableConsistencyLevelPolicy policy,
			String columnFamily, Composite start, ColumnSliceFinish finish, boolean reversed,
			int count)
	{
		this.policy = policy;
		this.columnFamily = columnFamily;
		this.start = start;
		this.finish = finish;
		this.reversed = reversed;
		this.count = count;
		this.readConsistencyLevelAtInitialization = policy.getCurrentReadLevel();
	}

	public interface ColumnSliceFinish
	{
		Composite function();
	}

	protected <T> T executeWithInitialConsistencyLevel(SafeExecutionContext<T> context)
	{
		T result = null;
		if (readConsistencyLevelAtInitialization != null)
		{
			ConsistencyLevel currentReadLevel = policy.getCurrentReadLevel();
			policy.setCurrentReadLevel(readConsistencyLevelAtInitialization);
			policy.loadConsistencyLevelForRead(columnFamily);
			try
			{
				result = context.execute();
				policy.setCurrentReadLevel(currentReadLevel);
			}
			catch (Throwable throwable)
			{
				policy.reinitCurrentConsistencyLevels();
				policy.reinitDefaultConsistencyLevels();
				throw new AchillesException(throwable);
			}
		}
		else
		{
			result = context.execute();
		}
		return result;

	}
}