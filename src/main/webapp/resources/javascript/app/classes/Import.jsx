import React from 'react';

export default class Import extends React.Component {
    constructor(pros) {
        super(pros);
        this.onChangeHandle = this.onChangeHandle.bind(this);
    }

    onChangeHandle(event) {
        event.target.form.submit();
    }

    render() {
        return (
            <form method="POST" encType="multipart/form-data">
                <input type="file" name="file"  onChange={this.onChangeHandle}/>
            </form>
        );
    }
}