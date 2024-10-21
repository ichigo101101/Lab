<template>
    <div>
        <div class="search">
            <el-input placeholder="请输入报修说明" style="width: 200px" v-model="name"></el-input>
            <el-button type="info" plain style="margin-left: 10px" @click="load(1)">查询</el-button>
            <el-button type="warning" plain style="margin-left: 10px" @click="reset">重置</el-button>
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini" v-if="user.role === 'ADMIN' && scope.row.status === '待处理'">处理</el-button>
        </div>

        <div class="table">
            <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"></el-table-column>
                <el-table-column prop="id" label="序号" width="80" align="center" sortable></el-table-column>
                <el-table-column prop="studentName" label="报修人" show-overflow-tooltip></el-table-column>
                <el-table-column prop="name" label="报修说明" show-overflow-tooltip></el-table-column>
                <el-table-column prop="time" label="报修时间"></el-table-column>
                <el-table-column prop="labName" label="实验室">
                    <template v-slot="scope">
                        {{scope.row.typeName}} - {{scope.row.labName}}
                    </template>
                </el-table-column>
                <el-table-column prop="labadminName" label="实验室管理员"></el-table-column>
                <el-table-column prop="status" label="处理状态"></el-table-column>
                <el-table-column prop="fixtime" label="处理时间"></el-table-column>

                <el-table-column label="操作" width="180" align="center">
                    <template v-slot="scope">
                        <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">编辑</el-button>
                        <el-button plain type="danger" size="mini" @click="del(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <el-dialog title="填写检修信息" :visible.sync="fromVisible" width="40%" :close-on-click-modal="false" destroy-on-close>
                <el-form label-width="100px" style="padding-right: 50px" :model="form" :rules="rules" ref="formRef">
                    <el-form-item prop="username" label="检修人">
                        <el-input v-model="form.username" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="phone" label="联系电话">
                        <el-input v-model="form.phone" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="content" label="检修说明">
                        <el-input v-model="form.content" autocomplete="off"></el-input>
                    </el-form-item>
                    <el-form-item prop="time" label="检修时间">
                        <el-date-picker style="width: 100%"
                                        v-model="form.time"
                                        type="datetime"
                                        value-format="yyyy-MM-dd HH:mm:ss"
                                        placeholder="选择日期时间">
                        </el-date-picker>
                    </el-form-item>
                    <el-form-item prop="department" label="检修单位">
                        <el-input v-model="form.department" autocomplete="off"></el-input>
                    </el-form-item>
                </el-form>
                <div slot="footer" class="dialog-footer">
                    <el-button @click="fromVisible = false">取 消</el-button>
                    <el-button type="primary" @click="save">确 定</el-button>
                </div>
            </el-dialog>

            <div class="pagination">
                <el-pagination
                        background
                        @current-change="handleCurrentChange"
                        :current-page="pageNum"
                        :page-sizes="[5, 10, 20]"
                        :page-size="pageSize"
                        layout="total, prev, pager, next"
                        :total="total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "Fix",
        data() {
            return {
                tableData: [],
                pageNum: 1,
                pageSize: 10,
                total: 0,
                name: null,
                fromVisible: false,
                form: {},
                user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
                rules: {
                    username: [
                        { required: true, message: '请输入检修人', trigger: 'blur' },
                    ],
                    content: [
                        { required: true, message: '请输入检修说明', trigger: 'blur' },
                    ]
                },
                ids: []
            }
        },
        created() {
            this.load(1);
        },
        methods: {
            save() {
                this.$refs.formRef.validate((valid) => {
                    if (valid) {
                        const url = this.form.id ? '/fix/update' : '/fix/add';
                        const method = this.form.id ? 'PUT' : 'POST';

                        this.$request({
                            url,
                            method,
                            data: this.form
                        }).then(res => {
                            if (res.code === '200') {
                                this.$message.success('保存成功');
                                this.load(1);
                                this.fromVisible = false;
                            } else {
                                this.$message.error(res.msg);
                            }
                        }).catch(err => {
                            this.$message.error('保存失败：' + err.message);
                        });
                    }
                });
            },
            handleAdd() {
                this.form = {};
                this.fromVisible = true;
            },
            handleEdit(row) {
                this.form = JSON.parse(JSON.stringify(row));
                this.fromVisible = true;
                this.id = row.id;
                this.time = row.time;
                this.form.labId = row.labId;
                this.form.fixId = row.id;
            },
            del(id) {
                this.$confirm('您确定删除吗？', '确认删除', { type: "warning" })
                    .then(() => {
                        this.$request.delete('/fix/delete/' + id)
                            .then(res => {
                                if (res.code === '200') {
                                    this.$message.success('操作成功');
                                    this.load(1);
                                } else {
                                    this.$message.error(res.msg);
                                }
                            }).catch(err => {
                            this.$message.error('删除失败：' + err.message);
                        });
                    }).catch(() => {});
            },
            handleSelectionChange(rows) {
                this.ids = rows.map(v => v.id);
            },
            delBatch() {
                if (!this.ids.length) {
                    this.$message.warning('请选择数据');
                    return;
                }
                this.$confirm('您确定批量删除这些数据吗？', '确认删除', { type: "warning" })
                    .then(() => {
                        this.$request.delete('/fix/delete/batch', { data: this.ids })
                            .then(res => {
                                if (res.code === '200') {
                                    this.$message.success('操作成功');
                                    this.load(1);
                                } else {
                                    this.$message.error(res.msg);
                                }
                            }).catch(err => {
                            this.$message.error('批量删除失败：' + err.message);
                        });
                    }).catch(() => {});
            },
            load(pageNum) {
                if (pageNum) this.pageNum = pageNum;
                this.$request.get('/fix/selectPage', {
                    params: {
                        pageNum: this.pageNum,
                        pageSize: this.pageSize,
                        name: this.name,
                    }
                }).then(res => {
                    this.tableData = res.data?.list;
                    this.total = res.data?.total;
                }).catch(err => {
                    this.$message.error('数据加载失败：' + err.message);
                });
            },
            reset() {
                this.name = null;
                this.load(1);
            },
            handleCurrentChange(pageNum) {
                this.load(pageNum);
            },
        }
    }
</script>

<style scoped>
    /* 这里可以添加样式 */
</style>
