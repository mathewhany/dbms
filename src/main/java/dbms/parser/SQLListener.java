// Generated from /Users/mathewhany/Code/dbms/SQL.g4 by ANTLR 4.12.0
package dbms.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SQLParser}.
 */
public interface SQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SQLParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(SQLParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(SQLParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#select}.
	 * @param ctx the parse tree
	 */
	void enterSelect(SQLParser.SelectContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#select}.
	 * @param ctx the parse tree
	 */
	void exitSelect(SQLParser.SelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#insert}.
	 * @param ctx the parse tree
	 */
	void enterInsert(SQLParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#insert}.
	 * @param ctx the parse tree
	 */
	void exitInsert(SQLParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#update}.
	 * @param ctx the parse tree
	 */
	void enterUpdate(SQLParser.UpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#update}.
	 * @param ctx the parse tree
	 */
	void exitUpdate(SQLParser.UpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_table}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table(SQLParser.Create_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_table}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table(SQLParser.Create_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_index}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index(SQLParser.Create_indexContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_index}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index(SQLParser.Create_indexContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#create_table_column}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_column(SQLParser.Create_table_columnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#create_table_column}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_column(SQLParser.Create_table_columnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(SQLParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(SQLParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#clustering_key}.
	 * @param ctx the parse tree
	 */
	void enterClustering_key(SQLParser.Clustering_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#clustering_key}.
	 * @param ctx the parse tree
	 */
	void exitClustering_key(SQLParser.Clustering_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#clustering_key_value}.
	 * @param ctx the parse tree
	 */
	void enterClustering_key_value(SQLParser.Clustering_key_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#clustering_key_value}.
	 * @param ctx the parse tree
	 */
	void exitClustering_key_value(SQLParser.Clustering_key_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#assignments}.
	 * @param ctx the parse tree
	 */
	void enterAssignments(SQLParser.AssignmentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#assignments}.
	 * @param ctx the parse tree
	 */
	void exitAssignments(SQLParser.AssignmentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(SQLParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(SQLParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#delete}.
	 * @param ctx the parse tree
	 */
	void enterDelete(SQLParser.DeleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#delete}.
	 * @param ctx the parse tree
	 */
	void exitDelete(SQLParser.DeleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 */
	void enterColumn(SQLParser.ColumnContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#column}.
	 * @param ctx the parse tree
	 */
	void exitColumn(SQLParser.ColumnContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(SQLParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#conditions}.
	 * @param ctx the parse tree
	 */
	void enterConditions(SQLParser.ConditionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#conditions}.
	 * @param ctx the parse tree
	 */
	void exitConditions(SQLParser.ConditionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#compound}.
	 * @param ctx the parse tree
	 */
	void enterCompound(SQLParser.CompoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#compound}.
	 * @param ctx the parse tree
	 */
	void exitCompound(SQLParser.CompoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(SQLParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(SQLParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(SQLParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(SQLParser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(SQLParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SQLParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(SQLParser.ValueContext ctx);
}